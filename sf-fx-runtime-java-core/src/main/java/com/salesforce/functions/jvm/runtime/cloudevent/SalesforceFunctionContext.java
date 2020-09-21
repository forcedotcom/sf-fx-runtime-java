package com.salesforce.functions.jvm.runtime.cloudevent;

import com.google.gson.annotations.SerializedName;

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
}
