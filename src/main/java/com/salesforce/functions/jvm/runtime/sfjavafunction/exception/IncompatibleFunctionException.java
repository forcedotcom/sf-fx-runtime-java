package com.salesforce.functions.jvm.runtime.sfjavafunction.exception;

public class IncompatibleFunctionException extends SalesforceFunctionException {
    public IncompatibleFunctionException(String message) {
        super(message);
    }

    public IncompatibleFunctionException(String message, Throwable cause) {
        super(message, cause);
    }
}
