/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiSupport.isOK;
import static com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiSupport.parseJsonErrors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class UpdateRecordRestApiRequest implements RestApiRequest<ModifyRecordResult> {
  private final String id;
  private final String type;
  private final Map<String, JsonElement> values;

  public UpdateRecordRestApiRequest(String id, String type, Map<String, JsonElement> values) {
    this.id = id;
    this.type = type;
    this.values = new HashMap<>(values);
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.PATCH;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "sobjects", this.type, this.id)
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
    return new ModifyRecordResult(id);
  }
}
