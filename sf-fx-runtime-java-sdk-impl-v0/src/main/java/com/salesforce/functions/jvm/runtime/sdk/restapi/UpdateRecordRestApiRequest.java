/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public class UpdateRecordRestApiRequest implements RestApiRequest<ModifyRecordResult> {
  private final String id;
  private final String type;
  private final Map<String, JsonPrimitive> values;

  public UpdateRecordRestApiRequest(String id, String type, Map<String, JsonPrimitive> values) {
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
  public Optional<JsonElement> getBody() {
    return Optional.of(new Gson().toJsonTree(values));
  }

  @Override
  public ModifyRecordResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement body) throws RestApiException {
    if (statusCode == 204) {
      return new ModifyRecordResult(id);
    } else {
      throw new RestApiException(ErrorResponseParser.parse(body));
    }
  }
}
