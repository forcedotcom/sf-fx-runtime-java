package com.salesforce.functions.jvm.runtime.sfjavafunction.api;

import com.google.common.reflect.AbstractInvocationHandler;
import com.salesforce.functions.jvm.runtime.sfjavafunction.cloudevent.extension.SalesforceContextCloudEventExtension;

import java.lang.reflect.Method;

public final class OrgContextInvocationHandler extends AbstractInvocationHandler {
    private final SalesforceContextCloudEventExtension salesforceContext;
    private final Object userContext;

    public OrgContextInvocationHandler(SalesforceContextCloudEventExtension salesforceContext, Object userContext) {
        this.salesforceContext = salesforceContext;
        this.userContext = userContext;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getId": return salesforceContext.getUserContext().getOrgId();
            case "getUser": return userContext;
            case "getBaseUrl": return salesforceContext.getUserContext().getSalesforceBaseUrl();
            case "getDomainUrl": return salesforceContext.getUserContext().getOrgDomainUrl();
            case "getApiVersion": return salesforceContext.getApiVersion();
            default:
                throw ApiUtil.createUnsupportedOperationException(method);
        }
    }
}
