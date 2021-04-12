/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.Org;
import io.cloudevents.CloudEvent;
import java.util.Optional;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ContextImpl implements Context {
  private final CloudEvent cloudEvent;
  private final OrgImpl org;

  public ContextImpl(
      CloudEvent cloudEvent,
      SalesforceContextCloudEventExtension salesforceContext,
      SalesforceFunctionContextCloudEventExtension functionContext) {
    this.cloudEvent = cloudEvent;
    this.org = new OrgImpl(salesforceContext, functionContext);
  }

  @Override
  @Nonnull
  public String getId() {
    return cloudEvent.getId();
  }

  @Override
  @Nonnull
  public Optional<Org> getOrg() {
    return Optional.of(org);
  }
}
