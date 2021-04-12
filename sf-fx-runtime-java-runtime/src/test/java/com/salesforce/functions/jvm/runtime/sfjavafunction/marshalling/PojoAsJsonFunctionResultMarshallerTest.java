/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.net.MediaType;
import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;

public class PojoAsJsonFunctionResultMarshallerTest {

  @Test
  public void testPojoWithoutAnnotations() throws Exception {
    FunctionResultMarshaller marshaller =
        new PojoAsJsonFunctionResultMarshaller(PojoWithoutAnnotations.class);
    PojoWithoutAnnotations data = new PojoWithoutAnnotations("Hello 👋🏻!");
    SalesforceFunctionResult result = marshaller.marshall(data);
    Assert.assertEquals(MediaType.JSON_UTF_8, result.getMediaType());
    Assert.assertArrayEquals(
        "{\"data\":\"Hello 👋🏻!\"}".getBytes(StandardCharsets.UTF_8), result.getData());
  }

  @Test
  public void testPojoWithGsonAnnotations() throws Exception {
    FunctionResultMarshaller marshaller =
        new PojoAsJsonFunctionResultMarshaller(PojoWithGsonAnnotations.class);
    PojoWithGsonAnnotations data = new PojoWithGsonAnnotations("Hello 👋🏻!");
    SalesforceFunctionResult result = marshaller.marshall(data);
    Assert.assertEquals(MediaType.JSON_UTF_8, result.getMediaType());
    Assert.assertArrayEquals(
        "{\"gsonData\":\"Hello 👋🏻!\"}".getBytes(StandardCharsets.UTF_8), result.getData());
  }

  @Test
  public void testPojoWithJacksonAnnotations() throws Exception {
    FunctionResultMarshaller marshaller =
        new PojoAsJsonFunctionResultMarshaller(PojoWithJacksonAnnotations.class);
    PojoWithJacksonAnnotations data = new PojoWithJacksonAnnotations("Hello 👋🏻!");
    SalesforceFunctionResult result = marshaller.marshall(data);
    Assert.assertEquals(MediaType.JSON_UTF_8, result.getMediaType());
    Assert.assertArrayEquals(
        "{\"jacksonData\":\"Hello 👋🏻!\"}".getBytes(StandardCharsets.UTF_8), result.getData());
  }

  @Test(expected = AmbiguousJsonLibraryException.class)
  public void testPojoWithAmbiguousJsonLibraryAnnotations() throws Exception {
    new PojoAsJsonFunctionResultMarshaller(PojoWithJacksonAndGsonAnnotations.class);
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
