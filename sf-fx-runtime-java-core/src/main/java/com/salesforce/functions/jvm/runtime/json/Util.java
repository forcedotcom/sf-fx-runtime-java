package com.salesforce.functions.jvm.runtime.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Util {
    public static List<Annotation> getAnnotationsOnClassFieldsAndMethods(Class<?> clazz) {
        ArrayList<Annotation> annotations = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            annotations.addAll(Arrays.asList(field.getAnnotations()));
        }

        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            annotations.addAll(Arrays.asList(method.getAnnotations()));
        }

        annotations.addAll(Arrays.asList(clazz.getAnnotations()));

        return annotations;
    }
}
