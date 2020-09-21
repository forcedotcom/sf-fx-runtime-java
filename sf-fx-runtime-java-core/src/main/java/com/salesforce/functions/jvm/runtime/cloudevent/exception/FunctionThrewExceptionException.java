package com.salesforce.functions.jvm.runtime.cloudevent.exception;

public class FunctionThrewExceptionException extends SalesforceCloudEventFunctionException {
    public FunctionThrewExceptionException(Throwable cause) {
        super(cause);
    }
}
