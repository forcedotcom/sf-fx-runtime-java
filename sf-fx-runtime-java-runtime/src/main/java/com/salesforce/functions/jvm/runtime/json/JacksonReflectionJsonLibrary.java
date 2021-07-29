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
import java.util.List;

public final class JacksonReflectionJsonLibrary implements JsonLibrary {
  private final Method readTreeMethod;
  private final Method atMethod;
  private final Method readerForMethod;
  private final Method readValueMethod;
  private final Method writeValueAsStringMethod;

  private final Package annotationsPackage;

  private final Object objectMapper;
  private final Method readValueListMethod;
  private final Method constructCollectionTypeMethod;
  private final Object typeFactory;

  public JacksonReflectionJsonLibrary(ClassLoader classLoader)
      throws JsonLibraryNotPresentException {
    try {
      Class<?> objectMapperClass =
          classLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
      Class<?> objectReaderClass =
          classLoader.loadClass("com.fasterxml.jackson.databind.ObjectReader");
      Class<?> jsonNodeClass = classLoader.loadClass("com.fasterxml.jackson.databind.JsonNode");
      Class<?> typeFactoryClass =
          classLoader.loadClass("com.fasterxml.jackson.databind.type.TypeFactory");
      Class<?> jacksonJavaTypeClass =
          classLoader.loadClass("com.fasterxml.jackson.databind.JavaType");

      readTreeMethod = objectMapperClass.getMethod("readTree", String.class);
      atMethod = jsonNodeClass.getMethod("at", String.class);
      readerForMethod = objectMapperClass.getMethod("readerFor", Class.class);
      readValueListMethod =
          objectMapperClass.getMethod("readValue", String.class, jacksonJavaTypeClass);
      readValueMethod = objectReaderClass.getMethod("readValue", jsonNodeClass);
      writeValueAsStringMethod = objectMapperClass.getMethod("writeValueAsString", Object.class);
      objectMapper = objectMapperClass.getConstructor().newInstance();

      typeFactory = objectMapperClass.getMethod("getTypeFactory", null).invoke(objectMapper);
      constructCollectionTypeMethod =
          typeFactoryClass.getMethod("constructCollectionType", Class.class, Class.class);

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
  @SuppressWarnings("unchecked")
  public List<Object> deserializeListAt(String json, Class<?> clazz, String... path)
      throws JsonDeserializationException {
    try {
      Object jsonNode = readTreeMethod.invoke(objectMapper, json);

      if (path.length > 0) {
        jsonNode = atMethod.invoke(jsonNode, "/" + String.join("/", path));
      }
      return (List<Object>)
          readValueListMethod.invoke(
              objectMapper,
              json,
              constructCollectionTypeMethod.invoke(
                  typeFactory,
                  List.class,
                  clazz)); // mapper.getTypeFactory().constructCollectionType(List.class, clazz)
    } catch (IllegalAccessException e) {
      throw new JsonDeserializationException(e);
    } catch (InvocationTargetException e) {
      throw new JsonDeserializationException(e.getCause());
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
