package com.salesforce.functions.jvm.runtime.cloudevent;

import java.util.Objects;

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

    public SalesforceContext(String apiVersion, String payloadVersion, UserContext userContext) {
        this.apiVersion = apiVersion;
        this.payloadVersion = payloadVersion;
        this.userContext = userContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesforceContext that = (SalesforceContext) o;
        return Objects.equals(apiVersion, that.apiVersion) &&
                Objects.equals(payloadVersion, that.payloadVersion) &&
                Objects.equals(userContext, that.userContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiVersion, payloadVersion, userContext);
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

        public UserContext(String orgId, String userId, String onBehalfOfUserId, String username, String salesforceBaseUrl, String orgDomainUrl) {
            this.orgId = orgId;
            this.userId = userId;
            this.onBehalfOfUserId = onBehalfOfUserId;
            this.username = username;
            this.salesforceBaseUrl = salesforceBaseUrl;
            this.orgDomainUrl = orgDomainUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserContext that = (UserContext) o;
            return Objects.equals(orgId, that.orgId) &&
                    Objects.equals(userId, that.userId) &&
                    Objects.equals(onBehalfOfUserId, that.onBehalfOfUserId) &&
                    Objects.equals(username, that.username) &&
                    Objects.equals(salesforceBaseUrl, that.salesforceBaseUrl) &&
                    Objects.equals(orgDomainUrl, that.orgDomainUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(orgId, userId, onBehalfOfUserId, username, salesforceBaseUrl, orgDomainUrl);
        }
    }
}
