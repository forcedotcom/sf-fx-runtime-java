package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.PayloadUnmarshallingException;
import io.cloudevents.CloudEvent;

public interface PayloadUnmarshaller {
    MediaType getHandledMediaType();
    Class<?> getTargetClass();
    Object unmarshall(CloudEvent cloudEvent) throws PayloadUnmarshallingException;
}
