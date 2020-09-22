package com.salesforce.functions.jvm.runtime.cloudevent;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

final class ProjectFunctionTestingHelper {

    public static <A, B> ProjectFunction createProjectFunction(Function<A, B> f, Class<A> a, Class<B> b) {
        try {
            Method applyMethod = f.getClass().getDeclaredMethod("apply", Object.class);
            return new ProjectFunction(TESTING_PROJECT, f, applyMethod, Collections.singletonList(a), b);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find apply(Object) method on function.", e);
        }
    }

    public static <A, B, C> ProjectFunction createProjectFunction(BiFunction<A, B, C> f, Class<A> a, Class<B> b, Class<C> c) {
        try {
            Method applyMethod = f.getClass().getDeclaredMethod("apply", Object.class, Object.class);

            ArrayList<Class<?>> parameterTypes = new ArrayList<>();
            parameterTypes.add(a);
            parameterTypes.add(b);

            return new ProjectFunction(TESTING_PROJECT, f, applyMethod, parameterTypes, c);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find apply(Object, Object) method on function.", e);
        }
    }

    private static Project TESTING_PROJECT = new Project() {
        @Override
        public String getTypeName() {
            return "Testing Project";
        }

        @Override
        public ClassLoader getClassLoader() {
            return getClass().getClassLoader();
        }
    };
}
