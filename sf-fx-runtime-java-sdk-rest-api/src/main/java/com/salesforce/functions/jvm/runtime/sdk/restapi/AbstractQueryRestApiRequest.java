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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;

public abstract class AbstractQueryRestApiRequest implements RestApiRequest<QueryRecordResult> {

  @Override
  public final QueryRecordResult processResponse(HttpResponse response)
      throws RestApiErrorsException, IOException, RestApiException {
    if (!isOK(response)) {
      throw parseJsonErrors(response);
    }

    final JsonElement json = parseJson(response);
    final JsonObject body = json.getAsJsonObject();
    final boolean done = body.get("done").getAsBoolean();
    final long totalSize = body.get("totalSize").getAsLong();
    final List<QueryRecord> records = new ArrayList<>();

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
          new Gson()
              .fromJson(attributesObject, new TypeToken<Map<String, JsonPrimitive>>() {}.getType());

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

      records.add(new QueryRecord(attributes, values));
    }

    return new QueryRecordResult(totalSize, done, records, nextRecordsPath);
  }
}
