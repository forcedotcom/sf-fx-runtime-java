package com.salesforce.functions.jvm.runtime.sfjavafunction.exception;

public class PayloadUnmarshallingException extends SalesforceFunctionException {
    public PayloadUnmarshallingException(String message) {
        super(message);
    }

    public PayloadUnmarshallingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayloadUnmarshallingException(Throwable cause) {
        super(cause);
    }
}
