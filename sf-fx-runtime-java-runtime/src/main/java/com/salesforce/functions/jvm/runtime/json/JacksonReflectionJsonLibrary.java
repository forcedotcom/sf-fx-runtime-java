/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonLibraryNotPresentException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonSerializationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class JacksonReflectionJsonLibrary implements JsonLibrary {
  private final Method readTreeMethod;
  private final Method atMethod;
  private final Method readerForMethod;
  private final Method readValueMethod;
  private final Method writeValueAsStringMethod;

  private final Package annotationsPackage;

  private final Object objectMapper;

  public JacksonReflectionJsonLibrary(ClassLoader classLoader)
      throws JsonLibraryNotPresentException {
    try {
      Class<?> objectMapperClass =
          classLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
      Class<?> objectReaderClass =
          classLoader.loadClass("com.fasterxml.jackson.databind.ObjectReader");
      Class<?> jsonNodeClass = classLoader.loadClass("com.fasterxml.jackson.databind.JsonNode");

      readTreeMethod = objectMapperClass.getMethod("readTree", String.class);
      atMethod = jsonNodeClass.getMethod("at", String.class);
      readerForMethod = objectMapperClass.getMethod("readerFor", Class.class);
      readValueMethod = objectReaderClass.getMethod("readValue", jsonNodeClass);
      writeValueAsStringMethod = objectMapperClass.getMethod("writeValueAsString", Object.class);

      objectMapper = objectMapperClass.getConstructor().newInstance();

      // Configure the ObjectMapper to not fail on empty beans
      Class<?> serializationFeatureClass =
          classLoader.loadClass("com.fasterxml.jackson.databind.SerializationFeature");
      Object failOnEmptyBeans =
          serializationFeatureClass.getField("FAIL_ON_EMPTY_BEANS").get(serializationFeatureClass);
      Method configureMethod =
          objectMapperClass.getMethod("configure", serializationFeatureClass, boolean.class);
      configureMethod.invoke(objectMapper, failOnEmptyBeans, false);

      Class<?> jsonValueAnnotationClass =
          classLoader.loadClass("com.fasterxml.jackson.annotation.JsonValue");
      annotationsPackage = jsonValueAnnotationClass.getPackage();
    } catch (NoSuchMethodException
        | ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchFieldException e) {
      throw new JsonLibraryNotPresentException(
          "Could not find expected Jackson classes/methods, Jackson support will not be enabled.",
          e);
    }
  }

  @Override
  public boolean mustBeUsedFor(Class<?> clazz) {
    for (Annotation annotation : Util.getAnnotationsOnClassFieldsAndMethods(clazz)) {
      if (annotation.annotationType().getPackage().equals(annotationsPackage)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Object deserializeAt(String json, Class<?> clazz, String... path)
      throws JsonDeserializationException {
    try {
      Object jsonNode = readTreeMethod.invoke(objectMapper, json);

      if (path.length > 0) {
        jsonNode = atMethod.invoke(jsonNode, "/" + String.join("/", path));
      }

      Object objectReader = readerForMethod.invoke(objectMapper, clazz);
      return readValueMethod.invoke(objectReader, jsonNode);
    } catch (IllegalAccessException e) {
      throw new JsonDeserializationException(e);
    } catch (InvocationTargetException e) {
      throw new JsonDeserializationException(e.getCause());
    }
  }

  @Override
  public String serialize(Object object) throws JsonSerializationException {
    try {
      return (String) writeValueAsStringMethod.invoke(objectMapper, object);
    } catch (IllegalAccessException e) {
      throw new JsonSerializationException(e);
    } catch (InvocationTargetException e) {
      throw new JsonSerializationException(e.getCause());
    }
  }
}
