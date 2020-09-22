package com.salesforce.functions.jvm.runtime.cloudevent;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public final class SalesforceFunctionContext {
    private String accessToken;
    private String functionInvocationId;
    private String functionName;
    private String apexClassId;
    @SerializedName("apexClassFQN")
    private String apexClassFqn;
    private String requestId;
    private String resource;

    public String getAccessToken() {
        return accessToken;
    }

    public String getFunctionInvocationId() {
        return functionInvocationId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getApexClassId() {
        return apexClassId;
    }

    public String getApexClassFqn() {
        return apexClassFqn;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getResource() {
        return resource;
    }

    public SalesforceFunctionContext(String accessToken, String functionInvocationId, String functionName, String apexClassId, String apexClassFqn, String requestId, String resource) {
        this.accessToken = accessToken;
        this.functionInvocationId = functionInvocationId;
        this.functionName = functionName;
        this.apexClassId = apexClassId;
        this.apexClassFqn = apexClassFqn;
        this.requestId = requestId;
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesforceFunctionContext that = (SalesforceFunctionContext) o;
        return Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(functionInvocationId, that.functionInvocationId) &&
                Objects.equals(functionName, that.functionName) &&
                Objects.equals(apexClassId, that.apexClassId) &&
                Objects.equals(apexClassFqn, that.apexClassFqn) &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, functionInvocationId, functionName, apexClassId, apexClassFqn, requestId, resource);
    }
}
