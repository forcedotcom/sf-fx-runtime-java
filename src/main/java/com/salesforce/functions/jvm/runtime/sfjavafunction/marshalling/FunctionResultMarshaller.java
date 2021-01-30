package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionResultMarshallingException;

public interface FunctionResultMarshaller {
    MediaType getMediaType();
    Class<?> getSourceClass();
    byte[] marshallBytes(Object object) throws FunctionResultMarshallingException;

    default SalesforceFunctionResult marshall(Object object) throws FunctionResultMarshallingException {
        return new SalesforceFunctionResult(getMediaType(), marshallBytes(object));
    }
}
