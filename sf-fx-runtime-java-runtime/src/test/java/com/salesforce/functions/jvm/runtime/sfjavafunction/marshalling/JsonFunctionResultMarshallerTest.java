/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.net.MediaType;
import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.json.ListParameterizedType;
import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class JsonFunctionResultMarshallerTest {

  @Test
  public void testPojoWithoutAnnotations() throws Exception {
    FunctionResultMarshaller marshaller =
        new JsonFunctionResultMarshaller(PojoWithoutAnnotations.class);
    PojoWithoutAnnotations data = new PojoWithoutAnnotations("Hello 👋🏻!");
    SalesforceFunctionResult result = marshaller.marshall(data);

    assertThat(result.getMediaType(), is(MediaType.JSON_UTF_8));

    assertThat(
        result.getData(),
        is(equalTo("{\"data\":\"Hello 👋🏻!\"}".getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  public void testPojoWithGsonAnnotations() throws Exception {
    FunctionResultMarshaller marshaller =
        new JsonFunctionResultMarshaller(PojoWithGsonAnnotations.class);
    PojoWithGsonAnnotations data = new PojoWithGsonAnnotations("Hello 👋🏻!");
    SalesforceFunctionResult result = marshaller.marshall(data);

    assertThat(result.getMediaType(), is(MediaType.JSON_UTF_8));
    assertThat(
        result.getData(),
        is(equalTo("{\"gsonData\":\"Hello 👋🏻!\"}".getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  public void testPojoWithJacksonAnnotations() throws Exception {
    FunctionResultMarshaller marshaller =
        new JsonFunctionResultMarshaller(PojoWithJacksonAnnotations.class);
    PojoWithJacksonAnnotations data = new PojoWithJacksonAnnotations("Hello 👋🏻!");
    SalesforceFunctionResult result = marshaller.marshall(data);

    assertThat(result.getMediaType(), is(MediaType.JSON_UTF_8));
    assertThat(
        result.getData(),
        is(equalTo("{\"jacksonData\":\"Hello 👋🏻!\"}".getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  public void testListOfPojoWithoutAnnotations() throws Exception {
    FunctionResultMarshaller marshaller =
        new JsonFunctionResultMarshaller(
            new ListParameterizedType(PojoWithoutAnnotations.class), getClass().getClassLoader());

    List<PojoWithoutAnnotations> data = new ArrayList<>();
    data.add(new PojoWithoutAnnotations("Hello 👋🏻!"));

    SalesforceFunctionResult result = marshaller.marshall(data);

    assertThat(result.getMediaType(), is(MediaType.JSON_UTF_8));

    assertThat(
        result.getData(),
        is(equalTo("[{\"data\":\"Hello 👋🏻!\"}]".getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  public void testListOfPojoWithGsonAnnotations() throws Exception {
    FunctionResultMarshaller marshaller =
        new JsonFunctionResultMarshaller(
            new ListParameterizedType(PojoWithGsonAnnotations.class), getClass().getClassLoader());

    List<PojoWithGsonAnnotations> data = new ArrayList<>();
    data.add(new PojoWithGsonAnnotations("Hello 👋🏻!"));

    SalesforceFunctionResult result = marshaller.marshall(data);

    assertThat(result.getMediaType(), is(MediaType.JSON_UTF_8));
    assertThat(
        result.getData(),
        is(equalTo("[{\"gsonData\":\"Hello 👋🏻!\"}]".getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  public void testListOfPojoWithJacksonAnnotations() throws Exception {
    FunctionResultMarshaller marshaller =
        new JsonFunctionResultMarshaller(
            new ListParameterizedType(PojoWithJacksonAnnotations.class),
            getClass().getClassLoader());

    List<PojoWithJacksonAnnotations> data = new ArrayList<>();
    data.add(new PojoWithJacksonAnnotations("Hello 👋🏻!"));

    SalesforceFunctionResult result = marshaller.marshall(data);

    assertThat(result.getMediaType(), is(MediaType.JSON_UTF_8));
    assertThat(
        result.getData(),
        is(equalTo("[{\"jacksonData\":\"Hello 👋🏻!\"}]".getBytes(StandardCharsets.UTF_8))));
  }

  @Test(expected = AmbiguousJsonLibraryException.class)
  public void testPojoWithAmbiguousJsonLibraryAnnotations() throws Exception {
    new JsonFunctionResultMarshaller(PojoWithJacksonAndGsonAnnotations.class);
  }

  static class PojoWithoutAnnotations {
    private final String data;

    public PojoWithoutAnnotations(String data) {
      this.data = data;
    }
  }

  static class PojoWithGsonAnnotations {
    @SerializedName("gsonData")
    private final String data;

    public PojoWithGsonAnnotations(String data) {
      this.data = data;
    }
  }

  static class PojoWithJacksonAnnotations {
    @JsonProperty("jacksonData")
    private final String data;

    public PojoWithJacksonAnnotations(String data) {
      this.data = data;
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
