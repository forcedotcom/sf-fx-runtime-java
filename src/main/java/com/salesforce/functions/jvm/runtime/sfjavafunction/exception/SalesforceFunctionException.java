package com.salesforce.functions.jvm.runtime.sfjavafunction.exception;

public class SalesforceFunctionException extends RuntimeException {
    public SalesforceFunctionException() {
    }

    public SalesforceFunctionException(String message) {
        super(message);
    }

    public SalesforceFunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SalesforceFunctionException(Throwable cause) {
        super(cause);
    }
}
