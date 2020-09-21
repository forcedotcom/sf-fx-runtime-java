package com.salesforce.functions.jvm.runtime.cloudevent.exception;

public class FunctionParameterJsonDeserializationException extends SalesforceCloudEventFunctionException {
    public FunctionParameterJsonDeserializationException(Throwable cause) {
        super(cause);
    }
}
