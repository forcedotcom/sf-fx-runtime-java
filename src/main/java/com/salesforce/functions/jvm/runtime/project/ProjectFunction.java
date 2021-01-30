package com.salesforce.functions.jvm.runtime.project;

public interface ProjectFunction<T, R, E extends Throwable> {
    String getName();
    R apply(T t) throws E;
}
