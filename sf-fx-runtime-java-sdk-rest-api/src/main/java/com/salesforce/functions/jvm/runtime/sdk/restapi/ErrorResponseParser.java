/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public final class ErrorResponseParser {

  public static List<RestApiError> parse(JsonElement json) {
    List<RestApiError> apiErrors = new ArrayList<>();
    for (JsonElement apiErrorElement : json.getAsJsonArray()) {

      JsonObject object = apiErrorElement.getAsJsonObject();
      String message = object.get("message").getAsString();
      String errorCode = object.get("errorCode").getAsString();

      List<String> fields = new ArrayList<>();
      JsonElement fieldsJsonElement = object.get("fields");
      if (fieldsJsonElement != null) {
        for (JsonElement field : fieldsJsonElement.getAsJsonArray()) {
          fields.add(field.getAsString());
        }
      }

      apiErrors.add(new RestApiError(message, errorCode, fields));
    }

    return apiErrors;
  }

  private ErrorResponseParser() {}
}
