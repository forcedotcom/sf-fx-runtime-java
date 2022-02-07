/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.net.URI;
import org.junit.Test;

public class ByteArrayPayloadUnmarshallerTest {

  @Test
  public void test() {
    PayloadUnmarshaller unmarshaller = new ByteArrayPayloadUnmarshaller();

    byte[] data = new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
    Object result = unmarshaller.unmarshall(createCloudEventWithData(data));

    assertThat(result, is(instanceOf(byte[].class)));
    assertThat(result, is(equalTo(data)));
  }

  @Test
  public void testWithoutData() {
    PayloadUnmarshaller unmarshaller = new ByteArrayPayloadUnmarshaller();

    CloudEvent cloudEvent =
        new CloudEventBuilder()
            .withId("id")
            .withSource(URI.create("urn:foo"))
            .withType("type")
            .build();

    Object result = unmarshaller.unmarshall(cloudEvent);

    assertThat(result, is(instanceOf(byte[].class)));
    assertThat(result, is(equalTo(new byte[0])));
  }

  private CloudEvent createCloudEventWithData(byte[] data) {
    return new CloudEventBuilder()
        .withId("id")
        .withSource(URI.create("urn:foo"))
        .withType("type")
        .withData(data)
        .build();
  }
}
