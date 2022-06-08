/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiSupport.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class CreateRecordRestApiRequest implements RestApiRequest<ModifyRecordResult> {
  private final String type;
  private final Map<String, JsonElement> values;

  public CreateRecordRestApiRequest(String type, Map<String, JsonElement> values) {
    this.type = type;
    this.values = values;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.POST;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "sobjects", type)
        .build();
  }

  @Override
  public Optional<HttpEntity> getBody() {
    return Optional.of(new StringEntity(new Gson().toJson(values), ContentType.APPLICATION_JSON));
  }

  @Override
  public ModifyRecordResult processResponse(HttpResponse response)
      throws RestApiErrorsException, IOException, RestApiException {
    if (!isOK(response)) {
      throw parseJsonErrors(response);
    }
    return new ModifyRecordResult(parseJson(response).getAsJsonObject().get("id").getAsString());
  }
}
