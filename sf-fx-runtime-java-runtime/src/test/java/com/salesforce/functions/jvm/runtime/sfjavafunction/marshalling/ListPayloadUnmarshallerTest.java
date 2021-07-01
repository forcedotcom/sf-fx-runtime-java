/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.PayloadUnmarshallingException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

public class ListPayloadUnmarshallerTest {

  @Test
  public void testString() {
    PayloadUnmarshaller unmarshaller = new ListPayloadUnmarshaller();

    byte[] data = "['foo', 'bar']".getBytes(StandardCharsets.UTF_8);

    List<String> result = (List<String>) unmarshaller.unmarshall(createCloudEventWithData(data));

    assertThat(result, is(instanceOf(List.class)));
    assertThat(result, hasItems("foo", "bar"));
  }

  @Test(expected = PayloadUnmarshallingException.class)
  public void testWithoutData() {
    PayloadUnmarshaller unmarshaller = new ListPayloadUnmarshaller();

    CloudEvent cloudEvent =
        new CloudEventBuilder()
            .withId("id")
            .withSource(URI.create("urn:foo"))
            .withType("type")
            .build();

    Object result = unmarshaller.unmarshall(cloudEvent);
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
