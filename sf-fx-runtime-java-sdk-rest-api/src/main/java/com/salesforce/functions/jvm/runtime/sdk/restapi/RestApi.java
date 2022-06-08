/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.HttpClients;

public final class RestApi {
  private final URI salesforceBaseUrl;
  private final String apiVersion;
  private final String accessToken;
  private final String clientVersion;

  public RestApi(URI salesforceBaseUrl, String apiVersion, String accessToken) {
    this.salesforceBaseUrl = salesforceBaseUrl;
    this.apiVersion = apiVersion;
    this.accessToken = accessToken;
    this.clientVersion = readVersionStringFromProperties().orElse("?.?.?-unknown");
  }

  public URI getSalesforceBaseUrl() {
    return salesforceBaseUrl;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public <T> T execute(RestApiRequest<T> apiRequest)
      throws RestApiErrorsException, RestApiException, IOException {
    HttpClient client = HttpClients.createDefault();
    HttpUriRequest request = createBaseHttpRequest(apiRequest);
    HttpResponse response = executeRequest(client, request);
    return apiRequest.processResponse(response);
  }

  private <T> URI createApiRequestUri(RestApiRequest<T> apiRequest) {
    try {
      return apiRequest.createUri(salesforceBaseUrl, apiVersion);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Unexpected URISyntaxException!", e);
    }
  }

  private HttpResponse executeRequest(HttpClient client, HttpUriRequest request)
      throws IOException {
    request.addHeader("Authorization", "Bearer " + accessToken);
    request.addHeader(
        "Sforce-Call-Options", "client=sf-fx-runtime-java-sdk-impl-v2:" + clientVersion);
    return client.execute(request);
  }

  private HttpUriRequest createBaseHttpRequest(RestApiRequest<?> apiRequest) {
    HttpMethod method = apiRequest.getHttpMethod();
    URI uri = createApiRequestUri(apiRequest);

    if (method == HttpMethod.GET) {
      return new HttpGet(uri);
    } else if (method == HttpMethod.DELETE) {
      return new HttpDelete(uri);
    }

    HttpEntityEnclosingRequestBase httpEntityEnclosingRequest;
    switch (method) {
      case POST:
        httpEntityEnclosingRequest = new HttpPost(uri);
        break;
      case PATCH:
        httpEntityEnclosingRequest = new HttpPatch(uri);
        break;
      case PUT:
        httpEntityEnclosingRequest = new HttpPut(uri);
        break;
      default:
        // Since we don't get exhaustive switch/cases (JEP 361, previews since Java 12+) we put
        // this as our own safeguard here. If another HttpMethod would be added, the code would
        // compile but at least fail with a useful exception at runtime. There is no way we can
        // get test coverage for this branch though.
        throw new RuntimeException("Unexpected HTTP method: " + method);
    }

    apiRequest.getBody().ifPresent(httpEntityEnclosingRequest::setEntity);

    return httpEntityEnclosingRequest;
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
