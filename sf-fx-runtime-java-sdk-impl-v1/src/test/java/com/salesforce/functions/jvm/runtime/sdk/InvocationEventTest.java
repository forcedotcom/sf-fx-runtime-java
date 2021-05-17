/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.net.URI;
import java.time.OffsetDateTime;
import org.junit.Test;

public class InvocationEventTest {

  @Test
  public void testInvocationEventWithoutOptionalValues() {
    CloudEvent cloudEvent =
        new CloudEventBuilder()
            .withId("id-1234-abc")
            .withSource(URI.create("urn:source"))
            .withType("type")
            .build();

    InvocationEventImpl<String> event = new InvocationEventImpl<>(cloudEvent, "Payload");

    assertThat(event.getData(), is(equalTo("Payload")));
    assertThat(event.getId(), is(equalTo(cloudEvent.getId())));
    assertThat(event.getSource(), is(equalTo(cloudEvent.getSource())));
    assertThat(event.getType(), is(equalTo(cloudEvent.getType())));
    assertThat(event.getDataContentType(), is(emptyOptional()));
    assertThat(event.getTime(), is(emptyOptional()));
    assertThat(event.getDataSchema(), is(emptyOptional()));
  }

  @Test
  public void testInvocationEventWithOptionalValues() {
    CloudEvent cloudEvent =
        new CloudEventBuilder()
            .withId("id-1234-abc")
            .withSource(URI.create("urn:source"))
            .withType("type")
            .withDataContentType("application/json")
            .withTime(OffsetDateTime.now())
            .withDataSchema(URI.create("urn:schema"))
            .build();

    InvocationEventImpl<String> event = new InvocationEventImpl<>(cloudEvent, "Payload");

    assertThat(event.getData(), is(equalTo("Payload")));
    assertThat(event.getId(), is(equalTo(cloudEvent.getId())));
    assertThat(event.getSource(), is(equalTo(cloudEvent.getSource())));
    assertThat(event.getType(), is(equalTo(cloudEvent.getType())));
    assertThat(
        event.getDataContentType(),
        is(optionalWithValue(equalTo(cloudEvent.getDataContentType()))));
    assertThat(event.getTime(), is(optionalWithValue(equalTo(cloudEvent.getTime()))));
    assertThat(event.getDataSchema(), is(optionalWithValue(equalTo(cloudEvent.getDataSchema()))));
  }
}
