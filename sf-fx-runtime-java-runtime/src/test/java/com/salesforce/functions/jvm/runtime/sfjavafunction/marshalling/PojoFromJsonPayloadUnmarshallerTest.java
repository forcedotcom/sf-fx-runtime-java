/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.PayloadUnmarshallingException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;

public class PojoFromJsonPayloadUnmarshallerTest {

  @Test
  public void test() throws Exception {
    PayloadUnmarshaller unmarshaller =
        new PojoFromJsonPayloadUnmarshaller(PojoWithoutAnnotations.class);

    Object result =
        unmarshaller.unmarshall(createCloudEventWithData("{\"data\": \"Hello 👋🏻!\"}"));

    Assert.assertTrue(result instanceof PojoWithoutAnnotations);
    Assert.assertEquals("Hello 👋🏻!", ((PojoWithoutAnnotations) result).getData());
  }

  @Test
  public void testGson() throws Exception {
    PayloadUnmarshaller unmarshaller =
        new PojoFromJsonPayloadUnmarshaller(PojoWithGsonAnnotations.class);

    Object result =
        unmarshaller.unmarshall(createCloudEventWithData("{\"gsonData\": \"Hello 👋🏻!\"}"));

    Assert.assertTrue(result instanceof PojoWithGsonAnnotations);
    Assert.assertEquals("Hello 👋🏻!", ((PojoWithGsonAnnotations) result).getData());
  }

  @Test
  public void testJackson() throws Exception {
    PayloadUnmarshaller unmarshaller =
        new PojoFromJsonPayloadUnmarshaller(PojoWithJacksonAnnotations.class);

    Object result =
        unmarshaller.unmarshall(createCloudEventWithData("{\"jacksonData\": \"Hello 👋🏻!\"}"));

    Assert.assertTrue(result instanceof PojoWithJacksonAnnotations);
    Assert.assertEquals("Hello 👋🏻!", ((PojoWithJacksonAnnotations) result).getData());
  }

  @Test(expected = AmbiguousJsonLibraryException.class)
  public void testAmbiguous() throws Exception {
    new PojoFromJsonPayloadUnmarshaller(PojoWithJacksonAndGsonAnnotations.class);
  }

  @Test(expected = PayloadUnmarshallingException.class)
  public void testWithoutData() throws Exception {
    PayloadUnmarshaller unmarshaller =
        new PojoFromJsonPayloadUnmarshaller(PojoWithoutAnnotations.class);

    CloudEvent cloudEvent =
        new CloudEventBuilder()
            .withId("id")
            .withSource(URI.create("urn:foo"))
            .withType("type")
            .build();

    unmarshaller.unmarshall(cloudEvent);
  }

  @Test(expected = PayloadUnmarshallingException.class)
  public void testWithInvalidJson() throws Exception {
    PayloadUnmarshaller unmarshaller =
        new PojoFromJsonPayloadUnmarshaller(PojoWithoutAnnotations.class);

    unmarshaller.unmarshall(createCloudEventWithData("garble"));
  }

  private CloudEvent createCloudEventWithData(String data) {
    return createCloudEventWithData(data.getBytes(StandardCharsets.UTF_8));
  }

  private CloudEvent createCloudEventWithData(byte[] data) {
    return new CloudEventBuilder()
        .withId("id")
        .withSource(URI.create("urn:foo"))
        .withType("type")
        .withData(data)
        .build();
  }

  static class PojoWithoutAnnotations {
    private final String data;

    public PojoWithoutAnnotations(String data) {
      this.data = data;
    }

    public String getData() {
      return data;
    }
  }

  static class PojoWithGsonAnnotations {
    @SerializedName("gsonData")
    private final String data;

    public PojoWithGsonAnnotations(String data) {
      this.data = data;
    }

    public String getData() {
      return data;
    }
  }

  static class PojoWithJacksonAnnotations {
    @JsonProperty("jacksonData")
    private String data;

    public PojoWithJacksonAnnotations() {}

    public PojoWithJacksonAnnotations(String data) {
      this.data = data;
    }

    public String getData() {
      return data;
    }
  }

  static class PojoWithJacksonAndGsonAnnotations {
    @JsonProperty("jacksonData")
    private final String data;

    @SerializedName("gsonData")
    private final String data2;

    public PojoWithJacksonAndGsonAnnotations(String data, String data2) {
      this.data = data;
      this.data2 = data2;
    }
  }
}
