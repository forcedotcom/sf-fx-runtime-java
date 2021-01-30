package com.salesforce.functions.jvm.runtime.sfjavafunction.api;

import com.google.common.reflect.AbstractInvocationHandler;
import com.salesforce.functions.jvm.runtime.sfjavafunction.cloudevent.extension.SalesforceContextCloudEventExtension;

import java.lang.reflect.Method;

public final class UserContextInvocationHandler extends AbstractInvocationHandler {
    private final SalesforceContextCloudEventExtension salesforceContext;

    public UserContextInvocationHandler(SalesforceContextCloudEventExtension salesforceContext) {
        this.salesforceContext = salesforceContext;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getId": return salesforceContext.getUserContext().getUserId();
            case "getUsername": return salesforceContext.getUserContext().getUsername();
            case "getOnBehalfOfUserId": return salesforceContext.getUserContext().getOnBehalfOfUserId();
            default:
                throw ApiUtil.createUnsupportedOperationException(method);
        }
    }
}
