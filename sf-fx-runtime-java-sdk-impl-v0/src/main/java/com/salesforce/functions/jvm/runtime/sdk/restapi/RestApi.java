/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class RestApi {
    private final URI salesforceBaseUrl;
    private final String apiVersion;
    private final String accessToken;
    private final Gson gson = new Gson();

    public RestApi(URI salesforceBaseUrl, String apiVersion, String accessToken) {
        this.salesforceBaseUrl = salesforceBaseUrl;
        this.apiVersion = apiVersion;
        this.accessToken = accessToken;
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

    public <T> T execute(RestApiRequest<T> apiRequest) throws IOException {
        URI uri = apiRequest.createUri(salesforceBaseUrl, apiVersion);

        HttpClient client = HttpClients.createDefault();

        HttpUriRequest request;
        if (apiRequest.getHttpMethod() == HttpMethod.GET) {
            request = new HttpGet(uri);
        } else if (apiRequest.getHttpMethod() == HttpMethod.POST || apiRequest.getHttpMethod() == HttpMethod.PATCH) {
            HttpEntityEnclosingRequestBase base;
            if (apiRequest.getHttpMethod() == HttpMethod.POST) {
                base = new HttpPost(uri);
            } else if (apiRequest.getHttpMethod() == HttpMethod.PATCH) {
                base = new HttpPatch(uri);
            } else {
                throw new IllegalStateException("Unexpected HTTP method " + apiRequest.getHttpMethod());
            }

            if (apiRequest.getBody().isPresent()) {
                base.setEntity(new StringEntity(gson.toJson(apiRequest.getBody().get()), ContentType.APPLICATION_JSON));
            }

            request = base;
        } else {
            throw new IllegalStateException("Unexpected HTTP method " + apiRequest.getHttpMethod());
        }

        request.addHeader("Authorization", "Bearer " + accessToken);
        HttpResponse response = client.execute(request);

        Map<String, String> headers = new HashMap<>();
        for (Header header : response.getAllHeaders()) {
            // This will overwrite header values for duplicate headers. This is intentional to be consistent
            // with the composite API that does not support multiple headers with the same name.
            headers.put(header.getName(), header.getValue());
        }

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return apiRequest.processResponse(response.getStatusLine().getStatusCode(), headers, null);
        } else {
            String bodyString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            JsonElement bodyJsonElement = gson.fromJson(bodyString, JsonElement.class);
            return apiRequest.processResponse(response.getStatusLine().getStatusCode(), headers, bodyJsonElement);
        }
    }
}
