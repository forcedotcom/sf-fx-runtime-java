/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public final class RestApi {
  private final URI orgDomainUrl;
  private final String apiVersion;
  private final String accessToken;
  private final String clientVersion;
  private final Gson gson = new Gson();

  public RestApi(URI orgDomainUrl, String apiVersion, String accessToken) {
    this.orgDomainUrl = orgDomainUrl;
    this.apiVersion = apiVersion;
    this.accessToken = accessToken;
    this.clientVersion = readVersionStringFromProperties().orElse("?.?.?-unknown");
  }

  public URI getOrgDomainUrl() {
    return orgDomainUrl;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public <T, A extends RestApiRequestBody, B> T execute(RestApiRequest<T, A, B> apiRequest)
      throws RestApiErrorsException, RestApiException, IOException {
    URI uri;
    try {
      uri = apiRequest.createUri(orgDomainUrl, apiVersion);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Unexpected URISyntaxException!", e);
    }

    HttpClient client = HttpClients.createDefault();

    HttpUriRequest request =
        createBaseHttpRequest(apiRequest.getHttpMethod(), uri, apiRequest.getBody());

    HttpResponse response = client.execute(request);

    Map<String, String> headers = new HashMap<>();
    for (Header header : response.getAllHeaders()) {
      // This will overwrite header values for duplicate headers. This is intentional and consistent
      // with the composite API that does not support multiple headers with the same name.
      headers.put(header.getName(), header.getValue());
    }

    HttpEntity entity = response.getEntity();
    if (entity == null) {
      return apiRequest.processResponse(response.getStatusLine().getStatusCode(), headers, null);
    } else {
      byte[] bodyBytes = EntityUtils.toByteArray(response.getEntity());

      try {
        return apiRequest.processResponse(
            response.getStatusLine().getStatusCode(), headers, apiRequest.parseBody(bodyBytes));
      } catch (BodyParsingException e) {
        throw new RestApiException(
            "Could not parse API response!\n" + Arrays.toString(bodyBytes), e);
      }
    }
  }

  public ByteBuffer downloadFile(String relativeUrl) throws URISyntaxException, IOException {
    URI uri = new URIBuilder(this.orgDomainUrl).setPath(relativeUrl).build();

    HttpUriRequest request = createBaseHttpRequest(HttpMethod.GET, uri, Optional.empty());

    HttpClient client = HttpClients.createDefault();
    return ByteBuffer.wrap(EntityUtils.toByteArray(client.execute(request).getEntity()));
  }

  private <A extends RestApiRequestBody> HttpUriRequest createBaseHttpRequest(
      HttpMethod method, URI uri, Optional<A> optionalBody) {

    HttpUriRequest request;

    if (method == HttpMethod.GET) {
      request = new HttpGet(uri);
    } else if (method == HttpMethod.DELETE) {
      request = new HttpDelete(uri);
    } else {
      HttpEntityEnclosingRequestBase httpEntityEnclosingRequest;
      switch (method) {
        case POST:
          httpEntityEnclosingRequest = new HttpPost(uri);
          break;
        case PATCH:
          httpEntityEnclosingRequest = new HttpPatch(uri);
          break;
        default:
          // Since we don't get exhaustive switch/cases (JEP 361, previews since Java 12+) we put
          // this as our own safeguard here. If another HttpMethod would be added, the code would
          // compile but at least fail with a useful exception at runtime. There is no way we can
          // get test coverage for this branch though.
          throw new RuntimeException("Unexpected HTTP method: " + method.toString());
      }

      optionalBody.ifPresent(
          body ->
              httpEntityEnclosingRequest.setEntity(
                  new ByteArrayEntity(body.getRequestContents(), body.getContentType())));

      request = httpEntityEnclosingRequest;
    }

    request.addHeader("Authorization", "Bearer " + accessToken);
    request.addHeader(
        "Sforce-Call-Options", "client=sf-fx-runtime-java-sdk-impl-v1:" + clientVersion);
    return request;
  }

  private Optional<String> readVersionStringFromProperties() {
    final Properties properties = new Properties();
    try (final InputStream stream =
        getClass().getClassLoader().getResourceAsStream("sf-fx-runtime-java-sdk-impl.properties")) {
      properties.load(stream);
      return Optional.ofNullable(properties.getProperty("version"));
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}
