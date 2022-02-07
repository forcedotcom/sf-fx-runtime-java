/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.json.ListParameterizedType;
import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.PayloadUnmarshallingException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

public class JsonPayloadUnmarshallerTest {

  @Test
  public void test() throws Exception {
    PayloadUnmarshaller unmarshaller = new JsonPayloadUnmarshaller(PojoWithoutAnnotations.class);

    Object result =
        unmarshaller.unmarshall(createCloudEventWithData("{\"data\": \"Hello 👋🏻!\"}"));

    assertThat(result, is(instanceOf(PojoWithoutAnnotations.class)));
    assertThat(((PojoWithoutAnnotations) result).getData(), is(equalTo("Hello 👋🏻!")));
  }

  @Test
  public void testGson() throws Exception {
    PayloadUnmarshaller unmarshaller = new JsonPayloadUnmarshaller(PojoWithGsonAnnotations.class);

    Object result =
        unmarshaller.unmarshall(createCloudEventWithData("{\"gsonData\": \"Hello 👋🏻!\"}"));

    assertThat(result, is(instanceOf(PojoWithGsonAnnotations.class)));
    assertThat(((PojoWithGsonAnnotations) result).getData(), is(equalTo("Hello 👋🏻!")));
  }

  @Test
  public void testJackson() throws Exception {
    PayloadUnmarshaller unmarshaller =
        new JsonPayloadUnmarshaller(PojoWithJacksonAnnotations.class);

    Object result =
        unmarshaller.unmarshall(createCloudEventWithData("{\"jacksonData\": \"Hello 👋🏻!\"}"));

    assertThat(result, is(instanceOf(PojoWithJacksonAnnotations.class)));
    assertThat(((PojoWithJacksonAnnotations) result).getData(), is(equalTo("Hello 👋🏻!")));
  }

  @Test
  public void testList() throws Exception {
    PayloadUnmarshaller unmarshaller =
        new JsonPayloadUnmarshaller(
            new ListParameterizedType(PojoWithoutAnnotations.class), getClass().getClassLoader());

    Object result =
        unmarshaller.unmarshall(createCloudEventWithData("[{\"data\": \"Hello 👋🏻!\"}]"));

    assertThat(
        ((List<?>) result),
        hasItems(pojo(Object.class).withProperty("data", equalTo("Hello 👋🏻!"))));
  }

  @Test
  public void testListGson() throws Exception {
    PayloadUnmarshaller unmarshaller =
        new JsonPayloadUnmarshaller(
            new ListParameterizedType(PojoWithGsonAnnotations.class), getClass().getClassLoader());

    Object result =
        unmarshaller.unmarshall(createCloudEventWithData("[{\"gsonData\": \"Hello 👋🏻!\"}]"));

    assertThat(
        ((List<?>) result),
        hasItems(pojo(Object.class).withProperty("data", equalTo("Hello 👋🏻!"))));
  }

  @Test
  public void testListJackson() throws Exception {
    PayloadUnmarshaller unmarshaller =
        new JsonPayloadUnmarshaller(
            new ListParameterizedType(PojoWithJacksonAnnotations.class),
            getClass().getClassLoader());

    Object result =
        unmarshaller.unmarshall(createCloudEventWithData("[{\"jacksonData\": \"Hello 👋🏻!\"}]"));

    assertThat(
        ((List<?>) result),
        hasItems(pojo(Object.class).withProperty("data", equalTo("Hello 👋🏻!"))));
  }

  @Test(expected = AmbiguousJsonLibraryException.class)
  public void testAmbiguous() throws Exception {
    new JsonPayloadUnmarshaller(PojoWithJacksonAndGsonAnnotations.class);
  }

  @Test(expected = PayloadUnmarshallingException.class)
  public void testWithoutData() throws Exception {
    PayloadUnmarshaller unmarshaller = new JsonPayloadUnmarshaller(PojoWithoutAnnotations.class);

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
    PayloadUnmarshaller unmarshaller = new JsonPayloadUnmarshaller(PojoWithoutAnnotations.class);

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
