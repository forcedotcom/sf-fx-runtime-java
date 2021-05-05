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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueryRestApiRequest implements RestApiRequest<QueryRecordResult> {

  @Override
  public final QueryRecordResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement json) throws RestApiErrorsException {

    final Gson gson = new Gson();

    if (statusCode != 200) {
      throw new RestApiErrorsException(ErrorResponseParser.parse(json));
    } else {
      final JsonObject body = json.getAsJsonObject();
      final boolean done = body.get("done").getAsBoolean();
      final long totalSize = body.get("totalSize").getAsLong();
      final List<Record> records = new ArrayList<>();

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
            gson.fromJson(
                attributesObject, new TypeToken<Map<String, JsonPrimitive>>() {}.getType());

        final Map<String, JsonPrimitive> values = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
          if (entry.getKey().equals("attributes")) {
            continue;
          }

          if (entry.getValue().isJsonPrimitive()) {
            values.put(entry.getKey(), entry.getValue().getAsJsonPrimitive());
          } else if (entry.getValue().isJsonNull()) {
            // We don't add the value if it's null.
          } else {
            throw new RuntimeException("Unexpected value in record response: " + entry.getValue());
          }
        }

        records.add(new Record(attributes, values));
      }

      return new QueryRecordResult(totalSize, done, records, nextRecordsPath);
    }
  }
}
