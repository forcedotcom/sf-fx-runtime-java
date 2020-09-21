package com.salesforce.functions.jvm.runtime.cloudevent;

public final class SalesforceContext {
    private String apiVersion;
    private String payloadVersion;
    private UserContext userContext;

    public String getApiVersion() {
        return apiVersion;
    }

    public String getPayloadVersion() {
        return payloadVersion;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public static final class UserContext {
        private String orgId;
        private String userId;
        private String onBehalfOfUserId;
        private String username;
        private String salesforceBaseUrl;
        private String orgDomainUrl;

        public String getOrgId() {
            return orgId;
        }

        public String getUserId() {
            return userId;
        }

        public String getOnBehalfOfUserId() {
            return onBehalfOfUserId;
        }

        public String getUsername() {
            return username;
        }

        public String getSalesforceBaseUrl() {
            return salesforceBaseUrl;
        }

        public String getOrgDomainUrl() {
            return orgDomainUrl;
        }
    }
}
