/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import java.util.Optional;

public class User implements com.salesforce.functions.jvm.sdk.User {
  private final SalesforceContextCloudEventExtension salesforceContext;

  public User(SalesforceContextCloudEventExtension salesforceContext) {
    this.salesforceContext = salesforceContext;
  }

  @Override
  public String getId() {
    return salesforceContext.getUserContext().getUserId();
  }

  @Override
  public String getUsername() {
    return salesforceContext.getUserContext().getUsername();
  }

  @Override
  public Optional<String> getOnBehalfOfUserId() {
    return Optional.ofNullable(salesforceContext.getUserContext().getOnBehalfOfUserId());
  }
}
