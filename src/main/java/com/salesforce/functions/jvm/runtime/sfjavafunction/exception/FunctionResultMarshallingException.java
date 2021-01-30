package com.salesforce.functions.jvm.runtime.sfjavafunction.exception;

public class FunctionResultMarshallingException extends SalesforceFunctionException {
    public FunctionResultMarshallingException() {
    }

    public FunctionResultMarshallingException(String message) {
        super(message);
    }

    public FunctionResultMarshallingException(String message, Throwable cause) {
        super(message, cause);
    }
}
