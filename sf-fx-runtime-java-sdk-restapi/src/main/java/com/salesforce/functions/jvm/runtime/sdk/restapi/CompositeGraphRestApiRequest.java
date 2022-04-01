/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public class CompositeGraphRestApiRequest<T> implements RestApiRequest<Map<String, T>> {
  private final Gson gson = new Gson();
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
  public Optional<JsonElement> getBody() {
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

      entry.getValue().getBody().ifPresent(jsonElement -> subrequestJson.add("body", jsonElement));

      subrequestJsonArray.add(subrequestJson);
    }

    JsonObject graph0Body = new JsonObject();
    graph0Body.add("graphId", new JsonPrimitive("graph0"));
    graph0Body.add("compositeRequest", subrequestJsonArray);

    JsonArray graphsArray = new JsonArray();
    graphsArray.add(graph0Body);

    JsonObject body = new JsonObject();
    body.add("graphs", graphsArray);

    return Optional.of(body);
  }

  @Override
  public Map<String, T> processResponse(
      int statusCode, Map<String, String> headers, JsonElement body) throws RestApiErrorsException {
    Map<String, T> result = new HashMap<>();

    if (statusCode == 200) {
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

      for (JsonElement response : compositeResponses) {
        JsonObject responseAsObject = response.getAsJsonObject();

        String referenceId = responseAsObject.get("referenceId").getAsString();
        int subrequestStatusCode = responseAsObject.get("httpStatusCode").getAsInt();
        Map<String, String> subrequestHeaders =
            gson.fromJson(
                responseAsObject.get("httpHeaders"),
                new TypeToken<Map<String, String>>() {}.getType());
        JsonElement subrequestBody = responseAsObject.get("body");

        try {
          result.put(
              referenceId,
              subrequests
                  .get(referenceId)
                  .processResponse(subrequestStatusCode, subrequestHeaders, subrequestBody));

        } catch (RestApiErrorsException e) {
          errors.addAll(e.getApiErrors());
        }
      }

      if (!errors.isEmpty()) {
        throw new RestApiErrorsException(errors);
      }

      return result;
    }

    throw new RestApiErrorsException(ErrorResponseParser.parse(body));
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
        throw new RuntimeException("Unexpected HTTP method: " + method.toString());
    }
  }
}
