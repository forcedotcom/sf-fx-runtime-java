/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import java.net.URI;
import javax.annotation.Nonnull;

public class Org implements com.salesforce.functions.jvm.sdk.Org {
  private final SalesforceContextCloudEventExtension salesforceContext;
  private final User user;
  private final DataApi dataApi;

  public Org(
      SalesforceContextCloudEventExtension salesforceContext,
      SalesforceFunctionContextCloudEventExtension functionContext) {
    this.salesforceContext = salesforceContext;
    this.dataApi =
        new DataApi(this.getBaseUrl(), this.getApiVersion(), functionContext.getAccessToken());
    this.user = new com.salesforce.functions.jvm.runtime.sdk.User(salesforceContext);
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
    return salesforceContext.getApiVersion();
  }

  @Override
  @Nonnull
  public DataApi getDataApi() {
    return dataApi;
  }

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.User getUser() {
    return user;
  }
}
