/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonSerializationException;
import java.lang.reflect.Type;

public final class GsonJsonLibrary implements JsonLibrary {
  private final Gson gson = new Gson();

  @Override
  public Object deserializeAt(String json, Type type, String... path)
      throws JsonDeserializationException {
    try {
      JsonElement jsonElement = JsonParser.parseString(json);

      if (path.length > 0) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        for (String pathItem : path) {
          jsonObject = jsonObject.get(pathItem).getAsJsonObject();
        }

        jsonElement = jsonObject;
      }

      return gson.fromJson(jsonElement, type);
    } catch (JsonSyntaxException e) {
      throw new JsonDeserializationException(e);
    }
  }

  @Override
  public String serialize(Object object) throws JsonSerializationException {
    try {
      return gson.toJson(object);
    } catch (JsonSyntaxException e) {
      throw new JsonSerializationException(e);
    }
  }

  @Override
  public boolean mustBeUsedFor(Type type) {
    Package annotationsPackage = SerializedName.class.getPackage();
    return Util.typeContainsAnnotationFromPackage(type, annotationsPackage);
  }
}
