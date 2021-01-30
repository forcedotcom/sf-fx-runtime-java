package com.salesforce.functions.jvm.runtime.json.exception;

public class JsonLibraryNotPresentException extends Exception {
    public JsonLibraryNotPresentException(String message, Throwable cause) {
        super(message, cause);
    }
}
