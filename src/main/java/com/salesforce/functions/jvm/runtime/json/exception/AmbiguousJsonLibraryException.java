package com.salesforce.functions.jvm.runtime.json.exception;

public class AmbiguousJsonLibraryException extends Exception {
    public AmbiguousJsonLibraryException(String message) {
        super(message);
    }
}
