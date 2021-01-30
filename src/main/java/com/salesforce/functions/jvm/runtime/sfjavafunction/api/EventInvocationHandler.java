package com.salesforce.functions.jvm.runtime.sfjavafunction.api;

import com.google.common.reflect.AbstractInvocationHandler;
import io.cloudevents.CloudEvent;

import java.lang.reflect.Method;
import java.util.Optional;

public final class EventInvocationHandler extends AbstractInvocationHandler {
    private final CloudEvent cloudEvent;
    private final Object payloadData;

    public EventInvocationHandler(CloudEvent cloudEvent, Object payloadData) {
        this.cloudEvent = cloudEvent;
        this.payloadData = payloadData;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getId": return cloudEvent.getId();
            case "getType": return cloudEvent.getType();
            case "getSource": return cloudEvent.getSource();
            case "getData": return payloadData;
            case "getDataContentType": return Optional.ofNullable(cloudEvent.getDataContentType());
            case "getDataSchema": return Optional.ofNullable(cloudEvent.getDataSchema());
            case "getTime": return Optional.ofNullable(cloudEvent.getTime());
            default:
                throw ApiUtil.createUnsupportedOperationException(method);
        }
    }
}
