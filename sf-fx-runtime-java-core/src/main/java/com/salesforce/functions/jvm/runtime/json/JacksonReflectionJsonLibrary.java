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

    public JacksonReflectionJsonLibrary(ClassLoader classLoader) throws JsonLibraryNotPresentException {
        try {
            Class<?> objectMapperClass = classLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
            Class<?> objectReaderClass = classLoader.loadClass("com.fasterxml.jackson.databind.ObjectReader");
            Class<?> jsonNodeClass = classLoader.loadClass("com.fasterxml.jackson.databind.JsonNode");

            readTreeMethod = objectMapperClass.getMethod("readTree", String.class);
            atMethod = jsonNodeClass.getMethod("at", String.class);
            readerForMethod = objectMapperClass.getMethod("readerFor", Class.class);
            readValueMethod = objectReaderClass.getMethod("readValue", jsonNodeClass);
            writeValueAsStringMethod = objectMapperClass.getMethod("writeValueAsString", Object.class);

            objectMapper = objectMapperClass.getConstructor().newInstance();

            Class<?> jsonValueAnnotationClass = classLoader.loadClass("com.fasterxml.jackson.annotation.JsonValue");
            annotationsPackage = jsonValueAnnotationClass.getPackage();
        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JsonLibraryNotPresentException("Could not find expected Jackson classes/methods, Jackson support will not be enabled.", e);
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
    public Object deserializeAt(String json, Class<?> clazz, String... path) throws JsonDeserializationException {
        try {
            Object jsonNode = readTreeMethod.invoke(objectMapper, json);
            jsonNode = atMethod.invoke(jsonNode, "/" + String.join("/", path));

            Object objectReader = readerForMethod.invoke(objectMapper, clazz);
            return readValueMethod.invoke(objectReader, jsonNode);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new JsonDeserializationException(e);
        }
    }

    @Override
    public String serialize(Object object) throws JsonSerializationException {
        try {
            return (String) writeValueAsStringMethod.invoke(objectMapper, object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JsonSerializationException(e);
        }
    }
}
