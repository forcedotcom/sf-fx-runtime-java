/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.JsonElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public class DeleteRecordRestApiRequest implements RestApiRequest<ModifyRecordResult> {
  private final String type;
  private final String id;

  public DeleteRecordRestApiRequest(String type, String id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.DELETE;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "sobjects", type, id)
        .build();
  }

  @Override
  public Optional<JsonElement> getBody() {
    return Optional.empty();
  }

  @Override
  public ModifyRecordResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement body) throws RestApiErrorsException {
    if (statusCode == 204) {
      return new ModifyRecordResult(id);
    } else {
      throw new RestApiErrorsException(ErrorResponseParser.parse(body));
    }
  }
}
