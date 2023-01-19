/*
 * Copyright (c) 2022, salesforce.com, inc.
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
  private final Gson gson = new Gson();

  @Override
  public final QueryRecordResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement json) throws RestApiErrorsException {

    if (statusCode != 200) {
      throw new RestApiErrorsException(ErrorResponseParser.parse(json));
    } else {
      return parseQueryResult(json.getAsJsonObject());
    }
  }

  private QueryRecordResult parseQueryResult(JsonObject body) {
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
      records.add(parseQueryRecord(jsonElement.getAsJsonObject()));
    }

    return new QueryRecordResult(totalSize, done, records, nextRecordsPath);
  }

  private Record parseQueryRecord(JsonObject data) {
    final JsonObject attributesObject = data.get("attributes").getAsJsonObject();
    final Map<String, JsonPrimitive> attributes =
        gson.fromJson(attributesObject, new TypeToken<Map<String, JsonPrimitive>>() {}.getType());

    final Map<String, Record.FieldValue> values = new HashMap<>();

    for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
      if (entry.getKey().equals("attributes")) {
        continue;
      }

      if (entry.getValue().isJsonPrimitive()) {
        values.put(entry.getKey(), new Record.FieldValue(entry.getValue().getAsJsonPrimitive()));
      } else if (entry.getValue().isJsonObject()) {
        JsonObject nestedJsonObject = entry.getValue().getAsJsonObject();
        if (nestedJsonObject.has("attributes")) {
          values.put(entry.getKey(), new Record.FieldValue(parseQueryRecord(nestedJsonObject)));
        } else {
          values.put(
              entry.getKey(),
              new Record.FieldValue(parseQueryResult(entry.getValue().getAsJsonObject())));
        }
      } else {
        // We don't throw an exception if the value is null, but it will not be added.
        if (!entry.getValue().isJsonNull()) {
          throw new RuntimeException("Unexpected value in record response: " + entry.getValue());
        }
      }
    }

    return new Record(attributes, values);
  }
}
