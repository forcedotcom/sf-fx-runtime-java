/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.requests;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.salesforce.functions.jvm.runtime.sdk.restapi.*;
import com.salesforce.functions.jvm.runtime.sdk.restapi.json.JsonRequestBody;
import com.salesforce.functions.jvm.runtime.sdk.restapi.json.JsonRestApiRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public class CreateRecordRestApiRequest extends JsonRestApiRequest<ModifyRecordResult> {
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
  public Optional<JsonRequestBody> getBody() {
    return Optional.of(new JsonRequestBody(new Gson().toJsonTree(values)));
  }

  @Override
  public ModifyRecordResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement jsonRequestBody)
      throws RestApiErrorsException {
    if (statusCode == 201) {
      return new ModifyRecordResult(jsonRequestBody.getAsJsonObject().get("id").getAsString());
    } else {
      throw new RestApiErrorsException(ErrorResponseParser.parse(jsonRequestBody));
    }
  }
}
