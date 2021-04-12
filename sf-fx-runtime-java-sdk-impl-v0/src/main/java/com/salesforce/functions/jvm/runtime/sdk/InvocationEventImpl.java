/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.sdk.InvocationEvent;
import io.cloudevents.CloudEvent;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class InvocationEventImpl<T> implements InvocationEvent<T> {
  private final CloudEvent cloudEvent;
  private final T payloadData;

  @SuppressWarnings("unused")
  public InvocationEventImpl(CloudEvent cloudEvent, T payloadData) {
    this.cloudEvent = cloudEvent;
    this.payloadData = payloadData;
  }

  @Override
  @Nonnull
  public String getId() {
    return cloudEvent.getId();
  }

  @Override
  @Nonnull
  public String getType() {
    return cloudEvent.getType();
  }

  @Override
  @Nonnull
  public URI getSource() {
    return cloudEvent.getSource();
  }

  @Override
  @Nonnull
  public T getData() {
    return payloadData;
  }

  @Override
  @Nonnull
  public Optional<String> getDataContentType() {
    return Optional.ofNullable(cloudEvent.getDataContentType());
  }

  @Override
  @Nonnull
  public Optional<URI> getDataSchema() {
    return Optional.ofNullable(cloudEvent.getDataSchema());
  }

  @Override
  @Nonnull
  public Optional<OffsetDateTime> getTime() {
    return Optional.ofNullable(cloudEvent.getTime());
  }
}
