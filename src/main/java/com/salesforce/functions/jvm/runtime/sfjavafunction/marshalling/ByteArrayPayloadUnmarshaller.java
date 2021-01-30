package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;

public class ByteArrayPayloadUnmarshaller implements PayloadUnmarshaller {
    @Override
    public MediaType getHandledMediaType() {
        return MediaType.ANY_TYPE;
    }

    @Override
    public Class<?> getTargetClass() {
        return byte[].class;
    }

    @Override
    public Object unmarshall(CloudEvent cloudEvent) {
        CloudEventData data = cloudEvent.getData();

        if (data != null) {
            return data.toBytes();
        }

        return new byte[0];
    }
}
