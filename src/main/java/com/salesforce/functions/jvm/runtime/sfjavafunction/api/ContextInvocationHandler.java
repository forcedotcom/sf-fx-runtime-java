package com.salesforce.functions.jvm.runtime.sfjavafunction.api;

import com.google.common.reflect.AbstractInvocationHandler;
import io.cloudevents.CloudEvent;

import java.lang.reflect.Method;

public final class ContextInvocationHandler extends AbstractInvocationHandler {
    private final CloudEvent cloudEvent;
    private final Object orgContext;

    public ContextInvocationHandler(CloudEvent cloudEvent, Object orgContext) {
        this.cloudEvent = cloudEvent;
        this.orgContext = orgContext;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getId": return cloudEvent.getId();
            case "getOrg": return orgContext;
            default:
                throw ApiUtil.createUnsupportedOperationException(method);
        }
    }
}
