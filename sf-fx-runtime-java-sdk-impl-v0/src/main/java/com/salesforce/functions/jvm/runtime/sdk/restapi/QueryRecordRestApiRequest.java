/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.apache.http.client.utils.URIBuilder;

public class QueryRecordRestApiRequest implements RestApiRequest<QueryRecordResult> {
  private final String soql;

  public QueryRecordRestApiRequest(String soql) {
    this.soql = soql;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) {
    try {
      return new URIBuilder(baseUri)
          .setPathSegments("services", "data", "v" + apiVersion, "query")
          .setParameter("q", soql)
          .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Unexpected URISyntaxException!", e);
    }
  }

  @Override
  public Optional<JsonElement> getBody() {
    return Optional.empty();
  }

  @Override
  public QueryRecordResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement json) {
    final Gson gson = new Gson();

    final JsonObject body = json.getAsJsonObject();
    final boolean done = body.get("done").getAsBoolean();
    final long totalSize = body.get("totalSize").getAsLong();
    final List<QueryRecordResult.Record> records = new ArrayList<>();

    final String nextRecordsPath;
    if (body.get("nextRecordsUrl") == null || body.get("nextRecordsUrl").isJsonNull()) {
      nextRecordsPath = null;
    } else {
      nextRecordsPath = body.get("nextRecordsUrl").getAsString();
    }

    for (JsonElement jsonElement : body.get("records").getAsJsonArray()) {
      final JsonObject jsonObject = jsonElement.getAsJsonObject();

      final JsonObject attributesObject = jsonObject.get("attributes").getAsJsonObject();
      final Map<String, JsonPrimitive> attributes =
          gson.fromJson(attributesObject, new TypeToken<Map<String, JsonPrimitive>>() {}.getType());

      final Map<String, JsonPrimitive> values = new HashMap<>();
      for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
        if (entry.getKey().equals("attributes")) {
          continue;
        }

        values.put(entry.getKey(), entry.getValue().getAsJsonPrimitive());
      }

      records.add(new QueryRecordResult.Record(attributes, values));
    }

    return new QueryRecordResult(totalSize, done, records, nextRecordsPath);
  }
}
