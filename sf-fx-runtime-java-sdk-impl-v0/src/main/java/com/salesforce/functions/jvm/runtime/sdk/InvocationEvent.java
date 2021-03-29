/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import io.cloudevents.CloudEvent;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;

public class InvocationEvent<T> implements com.salesforce.functions.jvm.sdk.InvocationEvent<T> {
  private final CloudEvent cloudEvent;
  private final T payloadData;

  public InvocationEvent(CloudEvent cloudEvent, T payloadData) {
    this.cloudEvent = cloudEvent;
    this.payloadData = payloadData;
  }

  @Override
  public String getId() {
    return cloudEvent.getId();
  }

  @Override
  public String getType() {
    return cloudEvent.getType();
  }

  @Override
  public URI getSource() {
    return cloudEvent.getSource();
  }

  @Override
  public T getData() {
    return payloadData;
  }

  @Override
  public Optional<String> getDataContentType() {
    return Optional.ofNullable(cloudEvent.getDataContentType());
  }

  @Override
  public Optional<URI> getDataSchema() {
    return Optional.ofNullable(cloudEvent.getDataSchema());
  }

  @Override
  public Optional<OffsetDateTime> getTime() {
    return Optional.ofNullable(cloudEvent.getTime());
  }
}
