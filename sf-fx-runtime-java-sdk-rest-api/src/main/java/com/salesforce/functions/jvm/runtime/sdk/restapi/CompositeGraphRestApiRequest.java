/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiSupport.*;

import com.google.gson.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.http.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

public class CompositeGraphRestApiRequest<T> implements RestApiRequest<Map<String, T>> {
  private final URI baseUri;
  private final String apiVersion;
  private final Map<String, RestApiRequest<T>> subrequests;

  public CompositeGraphRestApiRequest(
      URI baseUri, String apiVersion, Map<String, RestApiRequest<T>> subrequests) {
    this.baseUri = baseUri;
    this.apiVersion = apiVersion;
    this.subrequests = subrequests;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.POST;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "composite", "graph")
        .build();
  }

  @Override
  public Optional<HttpEntity> getBody() {
    JsonArray subrequestJsonArray = new JsonArray();

    for (Map.Entry<String, RestApiRequest<T>> entry : subrequests.entrySet()) {
      String method = httpMethodToCompositeMethodString(entry.getValue().getHttpMethod());

      // RestApiRequest#createUri returns an absolute URL. For a composite request, we need to strip
      // off the base URL from the returned value.
      final URI subrequestUrl;
      try {
        URI fullSubrequestUri = entry.getValue().createUri(baseUri, apiVersion);

        subrequestUrl =
            new URIBuilder()
                .setPath(fullSubrequestUri.getPath())
                .setCustomQuery(fullSubrequestUri.getQuery())
                .setFragment(fullSubrequestUri.getFragment())
                .build();

      } catch (URISyntaxException e) {
        throw new RuntimeException("Unexpected URISyntaxException!", e);
      }

      JsonObject subrequestJson = new JsonObject();
      subrequestJson.addProperty("method", method);
      subrequestJson.addProperty("url", subrequestUrl.toString());
      subrequestJson.addProperty("referenceId", entry.getKey());

      entry
          .getValue()
          .getBody()
          .ifPresent(
              httpEntity -> {
                try {
                  String jsonString = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
                  JsonElement jsonElement = new Gson().fromJson(jsonString, JsonElement.class);
                  subrequestJson.add("body", jsonElement);
                } catch (IOException ignored) {
                }
              });

      subrequestJsonArray.add(subrequestJson);
    }

    JsonObject graph0Body = new JsonObject();
    graph0Body.add("graphId", new JsonPrimitive("graph0"));
    graph0Body.add("compositeRequest", subrequestJsonArray);

    JsonArray graphsArray = new JsonArray();
    graphsArray.add(graph0Body);

    JsonObject body = new JsonObject();
    body.add("graphs", graphsArray);

    return Optional.of(new StringEntity(new Gson().toJson(body), ContentType.APPLICATION_JSON));
  }

  @Override
  public Map<String, T> processResponse(HttpResponse response)
      throws RestApiErrorsException, IOException, RestApiException {
    if (!isOK(response)) {
      throw parseJsonErrors(response);
    }

    Map<String, T> result = new HashMap<>();
    JsonElement body = parseJson(response);
    JsonArray graphsArray = body.getAsJsonObject().get("graphs").getAsJsonArray();
    if (graphsArray.size() != 1) {
      throw new IllegalStateException(
          "Composite REST API unexpectedly returned more or less than one graph!");
    }

    JsonObject graphObject = graphsArray.get(0).getAsJsonObject();

    JsonArray compositeResponses =
        graphObject
            .get("graphResponse")
            .getAsJsonObject()
            .get("compositeResponse")
            .getAsJsonArray();

    List<RestApiError> errors = new ArrayList<>();

    for (JsonElement compositeResponse : compositeResponses) {
      JsonObject responseAsObject = compositeResponse.getAsJsonObject();

      String referenceId = responseAsObject.get("referenceId").getAsString();
      int subrequestStatusCode = responseAsObject.get("httpStatusCode").getAsInt();
      JsonElement subrequestBody = responseAsObject.get("body");

      try {
        result.put(
            referenceId,
            processSubRequestResponse(referenceId, subrequestStatusCode, subrequestBody));
      } catch (RestApiErrorsException e) {
        errors.addAll(e.getApiErrors());
      }
    }

    if (!errors.isEmpty()) {
      throw new RestApiErrorsException(errors);
    }

    return result;
  }

  private T processSubRequestResponse(
      String referenceId, int subrequestStatusCode, JsonElement subrequestBody)
      throws RestApiErrorsException, IOException, RestApiException {
    SubRequestHttpResponse subRequestHttpResponse =
        new SubRequestHttpResponse(subrequestStatusCode, subrequestBody);
    return subrequests.get(referenceId).processResponse(subRequestHttpResponse);
  }

  private static String httpMethodToCompositeMethodString(HttpMethod method) {
    // We use an explicit mapping to strictly decouple internal representation from what is being
    // sent over to the API even though this currently is the default toString of the enum.
    switch (method) {
      case GET:
        return "GET";
      case POST:
        return "POST";
      case PATCH:
        return "PATCH";
      case DELETE:
        return "DELETE";
      default:
        // Since we don't get exhaustive switch/cases (JEP 361, previews since Java 12+) we put
        // this as our own safeguard here. If another HttpMethod would be added, the code would
        // compile but at least fail with a useful exception at runtime. There is no way we can
        // get test coverage for this branch though.
        throw new RuntimeException("Unexpected HTTP method: " + method);
    }
  }

  private static class SubRequestHttpResponse implements HttpResponse {
    private final StatusLine subRequestStatusLine;

    private final HttpEntity subRequestHttpEntity;

    public SubRequestHttpResponse(int subRequestStatusCode, JsonElement subRequestBody) {
      this.subRequestStatusLine = new SubRequestStatusLine(subRequestStatusCode);
      this.subRequestHttpEntity = new SubRequestHttpEntity(subRequestBody);
    }

    @Override
    public StatusLine getStatusLine() {
      return this.subRequestStatusLine;
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
      throw new UnsupportedOperationException();
    }

    @Override
    public HttpEntity getEntity() {
      return this.subRequestHttpEntity;
    }

    @Override
    public void setEntity(HttpEntity entity) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
      return null;
    }

    @Override
    public void setLocale(Locale loc) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
      return null;
    }

    @Override
    public boolean containsHeader(String name) {
      return false;
    }

    @Override
    public Header[] getHeaders(String name) {
      return new Header[0];
    }

    @Override
    public Header getFirstHeader(String name) {
      return null;
    }

    @Override
    public Header getLastHeader(String name) {
      return null;
    }

    @Override
    public Header[] getAllHeaders() {
      return new Header[0];
    }

    @Override
    public void addHeader(Header header) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(String name, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(Header header) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String name, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setHeaders(Header[] headers) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeHeader(Header header) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeHeaders(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HeaderIterator headerIterator() {
      return null;
    }

    @Override
    public HeaderIterator headerIterator(String name) {
      return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public org.apache.http.params.HttpParams getParams() {
      return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setParams(org.apache.http.params.HttpParams params) {
      throw new UnsupportedOperationException();
    }
  }

  private static class SubRequestStatusLine implements StatusLine {
    private final int subRequestStatusCode;

    public SubRequestStatusLine(int subRequestStatusCode) {
      this.subRequestStatusCode = subRequestStatusCode;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
      return null;
    }

    @Override
    public int getStatusCode() {
      return subRequestStatusCode;
    }

    @Override
    public String getReasonPhrase() {
      return null;
    }
  }

  private static class SubRequestHttpEntity implements HttpEntity {
    private final ByteArrayInputStream inputStream;

    public SubRequestHttpEntity(JsonElement json) {
      inputStream =
          new ByteArrayInputStream(new Gson().toJson(json).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public boolean isChunked() {
      return false;
    }

    @Override
    public long getContentLength() {
      return 0;
    }

    @Override
    public Header getContentType() {
      return new BasicHeader("Content-Type", "application/json; utf-8");
    }

    @Override
    public Header getContentEncoding() {
      return null;
    }

    @Override
    public InputStream getContent() throws UnsupportedOperationException {
      return this.inputStream;
    }

    @Override
    public void writeTo(OutputStream outStream) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStreaming() {
      return false;
    }

    @Override
    public void consumeContent() {
      throw new UnsupportedOperationException();
    }
  }
}
