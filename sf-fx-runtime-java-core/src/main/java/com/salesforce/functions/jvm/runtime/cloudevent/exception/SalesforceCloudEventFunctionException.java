package com.salesforce.functions.jvm.runtime.cloudevent.exception;

public class SalesforceCloudEventFunctionException extends RuntimeException {
    public SalesforceCloudEventFunctionException() {
    }

    public SalesforceCloudEventFunctionException(String message) {
        super(message);
    }

    public SalesforceCloudEventFunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SalesforceCloudEventFunctionException(Throwable cause) {
        super(cause);
    }
}
