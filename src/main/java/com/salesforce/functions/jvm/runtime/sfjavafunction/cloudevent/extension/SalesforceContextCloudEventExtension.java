package com.salesforce.functions.jvm.runtime.sfjavafunction.cloudevent.extension;

import java.net.URI;
import java.util.Objects;

public final class SalesforceContextCloudEventExtension {
    private final String apiVersion;
    private final String payloadVersion;
    private final UserContext userContext;

    public String getApiVersion() {
        return apiVersion;
    }

    public String getPayloadVersion() {
        return payloadVersion;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public SalesforceContextCloudEventExtension(String apiVersion, String payloadVersion, UserContext userContext) {
        this.apiVersion = apiVersion;
        this.payloadVersion = payloadVersion;
        this.userContext = userContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesforceContextCloudEventExtension that = (SalesforceContextCloudEventExtension) o;
        return Objects.equals(apiVersion, that.apiVersion) &&
                Objects.equals(payloadVersion, that.payloadVersion) &&
                Objects.equals(userContext, that.userContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiVersion, payloadVersion, userContext);
    }

    public static final class UserContext {
        private final String orgId;
        private final String userId;
        private final String onBehalfOfUserId;
        private final String username;
        private final URI salesforceBaseUrl;
        private final URI orgDomainUrl;

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

        public URI getSalesforceBaseUrl() {
            return salesforceBaseUrl;
        }

        public URI getOrgDomainUrl() {
            return orgDomainUrl;
        }

        public UserContext(String orgId, String userId, String onBehalfOfUserId, String username, URI salesforceBaseUrl, URI orgDomainUrl) {
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
