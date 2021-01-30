package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionResultMarshallingException;

public class ByteArrayFunctionResultMarshaller implements FunctionResultMarshaller {
    @Override
    public MediaType getMediaType() {
        return MediaType.OCTET_STREAM;
    }

    @Override
    public Class<?> getSourceClass() {
        return byte[].class;
    }

    @Override
    public byte[] marshallBytes(Object object) {
        if (!(object instanceof byte[])) {
            throw new FunctionResultMarshallingException(
                    String.format("Expected byte array for marshalling, got %s!", object.getClass().getName())
            );
        }

        return (byte[]) object;
    }
}
