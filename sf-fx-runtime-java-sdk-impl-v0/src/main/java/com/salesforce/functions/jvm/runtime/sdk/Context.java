/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import io.cloudevents.CloudEvent;
import java.util.Optional;

public class Context implements com.salesforce.functions.jvm.sdk.Context {
  private final CloudEvent cloudEvent;
  private final Org org;

  public Context(
      CloudEvent cloudEvent,
      SalesforceContextCloudEventExtension salesforceContext,
      SalesforceFunctionContextCloudEventExtension functionContext) {
    this.cloudEvent = cloudEvent;
    this.org = new Org(salesforceContext, functionContext);
  }

  @Override
  public String getId() {
    return cloudEvent.getId();
  }

  @Override
  public Optional<com.salesforce.functions.jvm.sdk.Org> getOrg() {
    return Optional.of(org);
  }
}
