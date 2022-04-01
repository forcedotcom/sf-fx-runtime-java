/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import java.util.Optional;
import javax.annotation.Nonnull;

public class UserImpl implements com.salesforce.functions.jvm.sdk.User {
  private final SalesforceContextCloudEventExtension salesforceContext;

  public UserImpl(SalesforceContextCloudEventExtension salesforceContext) {
    this.salesforceContext = salesforceContext;
  }

  @Override
  @Nonnull
  public String getId() {
    return salesforceContext.getUserContext().getUserId();
  }

  @Override
  @Nonnull
  public String getUsername() {
    return salesforceContext.getUserContext().getUsername();
  }

  @Override
  @Nonnull
  public Optional<String> getOnBehalfOfUserId() {
    return Optional.ofNullable(salesforceContext.getUserContext().getOnBehalfOfUserId());
  }
}
