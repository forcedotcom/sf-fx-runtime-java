package com.salesforce.functions.jvm.runtime.project;

public class ProjectBuilderException extends Exception {
    public ProjectBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectBuilderException(String message) {
        super(message);
    }
}
