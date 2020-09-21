package com.salesforce.functions.jvm.runtime.project;

public interface Project {
    String getTypeName();
    ClassLoader getClassLoader();
}
