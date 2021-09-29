/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.User;
import java.net.URI;
import javax.annotation.Nonnull;

public class OrgImpl implements Org {
  private final SalesforceContextCloudEventExtension salesforceContext;
  private final UserImpl user;
  private final DataApiImpl dataApi;

  public OrgImpl(
      SalesforceContextCloudEventExtension salesforceContext,
      SalesforceFunctionContextCloudEventExtension functionContext) {
    this.salesforceContext = salesforceContext;
    this.dataApi =
        new DataApiImpl(this.getBaseUrl(), this.getApiVersion(), functionContext.getAccessToken());
    this.user = new UserImpl(salesforceContext);
  }

  @Override
  @Nonnull
  public String getId() {
    return salesforceContext.getUserContext().getOrgId();
  }

  @Override
  @Nonnull
  public URI getBaseUrl() {
    return salesforceContext.getUserContext().getSalesforceBaseUrl();
  }

  @Override
  @Nonnull
  public URI getDomainUrl() {
    return salesforceContext.getUserContext().getOrgDomainUrl();
  }

  @Override
  @Nonnull
  public String getApiVersion() {
    // An API version is also available in the context via #getApiVersion(). That value differs
    // between orgs and can change seemingly randomly. To avoid surprises at runtime, we
    // intentionally don't use that value and instead fix the version.
    return "53.0";
  }

  @Override
  @Nonnull
  public DataApiImpl getDataApi() {
    return dataApi;
  }

  @Override
  @Nonnull
  public User getUser() {
    return user;
  }
}
