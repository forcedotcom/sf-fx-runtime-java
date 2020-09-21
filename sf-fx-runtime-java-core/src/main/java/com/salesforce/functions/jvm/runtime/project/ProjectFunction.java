package com.salesforce.functions.jvm.runtime.project;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public final class ProjectFunction {
    private Project project;
    private Object functionInstance;
    private Method functionMethod;
    private List<Class<?>> parameterTypes;
    private Class<?> returnType;

    public ProjectFunction(Project project, Object functionInstance, Method functionMethod, List<Class<?>> parameterTypes, Class<?> returnType) {
        this.project = project;
        this.functionInstance = functionInstance;
        this.functionMethod = functionMethod;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    public String getName() {
        return functionInstance.getClass().getName();
    }

    public int getArity() {
        return parameterTypes.size();
    }

    public boolean isUnary() {
        return parameterTypes.size() == 1;
    }

    public boolean isBinary() {
        return parameterTypes.size() == 2;
    }

    public Project getProject() {
        return project;
    }

    public List<Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Object apply(Object... parameters) throws InvocationTargetException, IllegalAccessException {
        functionMethod.setAccessible(true);
        return functionMethod.invoke(functionInstance, parameters);
    }
}
