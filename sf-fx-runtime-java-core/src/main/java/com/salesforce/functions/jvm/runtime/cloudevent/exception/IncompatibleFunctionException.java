package com.salesforce.functions.jvm.runtime.cloudevent.exception;

public class IncompatibleFunctionException extends SalesforceCloudEventFunctionException {
    public IncompatibleFunctionException(String message) {
        super(message);
    }

    public IncompatibleFunctionException(String message, Throwable cause) {
        super(message, cause);
    }
}
