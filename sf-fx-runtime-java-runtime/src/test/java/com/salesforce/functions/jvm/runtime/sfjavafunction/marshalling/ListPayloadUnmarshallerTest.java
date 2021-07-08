/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.PayloadUnmarshallingException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

public class ListPayloadUnmarshallerTest {
  @Test
  public void testInteger() throws AmbiguousJsonLibraryException {
    PayloadUnmarshaller unmarshaller = new ListPayloadUnmarshaller(Integer.class);

    byte[] data = "[1, 2, 3]".getBytes(StandardCharsets.UTF_8);

    List<Integer> result = (List<Integer>) unmarshaller.unmarshall(createCloudEventWithData(data));

    assertThat(result, is(instanceOf(List.class)));
    assertThat(result.get(0), is(instanceOf(Integer.class)));
  }

  @Test
  public void testDouble() throws AmbiguousJsonLibraryException {
    PayloadUnmarshaller unmarshaller = new ListPayloadUnmarshaller(Double.class);

    byte[] data = "[1.2, 1.3]".getBytes(StandardCharsets.UTF_8);

    List<Double> result = (List<Double>) unmarshaller.unmarshall(createCloudEventWithData(data));

    assertThat(result, is(instanceOf(List.class)));
    assertThat(result.get(0), is(instanceOf(Double.class)));
  }

  @Test
  public void testString() throws AmbiguousJsonLibraryException {
    PayloadUnmarshaller unmarshaller = new ListPayloadUnmarshaller(String.class);

    byte[] data = "['foo', 'bar']".getBytes(StandardCharsets.UTF_8);

    List<String> result = (List<String>) unmarshaller.unmarshall(createCloudEventWithData(data));

    assertThat(result, is(instanceOf(List.class)));
    assertThat(result, hasItems("foo", "bar"));
  }

  @Test(expected = PayloadUnmarshallingException.class)
  public void testWithoutData() throws AmbiguousJsonLibraryException {
    PayloadUnmarshaller unmarshaller = new ListPayloadUnmarshaller(String.class);

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
