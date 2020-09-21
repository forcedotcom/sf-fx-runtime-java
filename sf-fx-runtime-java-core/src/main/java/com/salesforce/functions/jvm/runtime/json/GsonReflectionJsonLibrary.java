package com.salesforce.functions.jvm.runtime.json;

import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonLibraryNotPresentException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonSerializationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
            fromJsonMethod = gsonClass.getMethod("fromJson", jsonElementClass, Class.class);
            toJsonMethod = gsonClass.getMethod("toJson", Object.class);

            gson = gsonClass.getConstructor().newInstance();

            Class<?> serializedNameAnnotationClass = classLoader.loadClass("com.google.gson.annotations.SerializedName");
            annotationsPackage = serializedNameAnnotationClass.getPackage();

        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JsonLibraryNotPresentException("Could not find expected GSON classes/methods, GSON support will not be enabled.", e);
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
            Object jsonElement = getAsJsonObjectMethod.invoke(parseStringMethod.invoke(null, json));
            for (String pathItem : path) {
                jsonElement = getAsJsonObjectMethod.invoke(getMethod.invoke(jsonElement, pathItem));
            }

            return fromJsonMethod.invoke(gson, jsonElement, clazz);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new JsonDeserializationException(e);
        }
    }

    @Override
    public String serialize(Object object) throws JsonSerializationException {
        try {
            return (String) toJsonMethod.invoke(gson, object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JsonSerializationException(e);
        }
    }
}
