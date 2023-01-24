/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.requests;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.salesforce.functions.jvm.runtime.sdk.restapi.*;
import com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.BulkIngestOperation;
import com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.ModifyBulkIngestJobResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.json.JsonRequestBody;
import com.salesforce.functions.jvm.runtime.sdk.restapi.json.JsonRestApiRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public final class CreateBulkIngestJobApiRequest
    extends JsonRestApiRequest<ModifyBulkIngestJobResult> {
  private final String objectType;
  private final BulkIngestOperation operation;

  public CreateBulkIngestJobApiRequest(String objectType, BulkIngestOperation operation) {
    this.objectType = objectType;
    this.operation = operation;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.POST;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "jobs", "ingest")
        .build();
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    JsonObject body = new JsonObject();
    body.addProperty("object", objectType);

    switch (operation) {
      case INSERT:
        body.addProperty("operation", "insert");
        break;
      case UPDATE:
        body.addProperty("operation", "update");
        break;
      case UPSERT:
        body.addProperty("operation", "upsert");
        break;
      case DELETE:
        body.addProperty("operation", "delete");
        break;
      case HARD_DELETE:
        body.addProperty("operation", "hard_delete");
        break;
      default:
        throw new IllegalStateException("Unknown bulk ingest operation: " + operation);
    }

    return Optional.of(new JsonRequestBody(new Gson().toJsonTree(body)));
  }

  @Override
  public ModifyBulkIngestJobResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement body) throws RestApiErrorsException {
    if (statusCode == 200) {
      return new ModifyBulkIngestJobResult(body.getAsJsonObject().get("id").getAsString());
    } else {
      throw new RestApiErrorsException(ErrorResponseParser.parse(body));
    }
  }
}
