/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonLibraryNotPresentException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonSerializationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public final class GsonReflectionJsonLibrary implements JsonLibrary {
  private final Method parseStringMethod;
  private final Method getAsJsonObjectMethod;
  private final Method getMethod;
  private final Method fromJsonMethod;
  private final Method toJsonMethod;

  private final Package annotationsPackage;

  private final Object gson;

  public GsonReflectionJsonLibrary(ClassLoader classLoader) throws JsonLibraryNotPresentException {
    try {
      Class<?> jsonParserClass = classLoader.loadClass("com.google.gson.JsonParser");
      Class<?> jsonElementClass = classLoader.loadClass("com.google.gson.JsonElement");
      Class<?> jsonObjectClass = classLoader.loadClass("com.google.gson.JsonObject");
      Class<?> gsonClass = classLoader.loadClass("com.google.gson.Gson");

      parseStringMethod = jsonParserClass.getMethod("parseString", String.class);
      getAsJsonObjectMethod = jsonElementClass.getMethod("getAsJsonObject");
      getMethod = jsonObjectClass.getMethod("get", String.class);
      fromJsonMethod = gsonClass.getMethod("fromJson", jsonElementClass, Type.class);
      toJsonMethod = gsonClass.getMethod("toJson", Object.class);

      gson = gsonClass.getConstructor().newInstance();

      Class<?> serializedNameAnnotationClass =
          classLoader.loadClass("com.google.gson.annotations.SerializedName");
      annotationsPackage = serializedNameAnnotationClass.getPackage();

    } catch (NoSuchMethodException
        | ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new JsonLibraryNotPresentException(
          "Could not find expected GSON classes/methods, GSON support will not be enabled.", e);
    }
  }

  @Override
  public boolean mustBeUsedFor(Type type) {
    return Util.typeContainsAnnotationFromPackage(type, annotationsPackage);
  }

  @Override
  public Object deserializeAt(String json, Type type, String... path)
      throws JsonDeserializationException {
    try {
      Object jsonElement = parseStringMethod.invoke(null, json);

      if (path.length > 0) {
        jsonElement = getAsJsonObjectMethod.invoke(jsonElement);
        for (String pathItem : path) {
          jsonElement = getAsJsonObjectMethod.invoke(getMethod.invoke(jsonElement, pathItem));
        }
      }

      return fromJsonMethod.invoke(gson, jsonElement, type);
    } catch (IllegalAccessException e) {
      throw new JsonDeserializationException(e);
    } catch (InvocationTargetException e) {
      throw new JsonDeserializationException(e.getCause());
    }
  }

  @Override
  public String serialize(Object object) throws JsonSerializationException {
    try {
      return (String) toJsonMethod.invoke(gson, object);
    } catch (IllegalAccessException e) {
      throw new JsonSerializationException(e);
    } catch (InvocationTargetException e) {
      throw new JsonSerializationException(e.getCause());
    }
  }
}
