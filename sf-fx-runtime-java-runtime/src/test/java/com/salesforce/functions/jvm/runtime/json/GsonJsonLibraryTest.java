/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonSerializationException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.Test;

public class GsonJsonLibraryTest {

  @Test
  public void testDeserialization() throws Exception {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    Object testClass = jsonLibrary.deserializeAt("{\"foo\": \"bar\"}", TestClass.class);

    assertThat(((TestClass) testClass).getFoo(), is(equalTo("bar")));
  }

  @Test(expected = JsonDeserializationException.class)
  public void testExceptionWrapping() throws Exception {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    jsonLibrary.deserializeAt("{\"foo: \"bar\"}", Test.class);
  }

  @Test
  public void testDeserializationWithPath() throws Exception {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    Object testClass =
        jsonLibrary.deserializeAt("{\"inner\": {\"foo\": \"bar\"}}", TestClass.class, "inner");

    assertThat(((TestClass) testClass).getFoo(), is(equalTo("bar")));
  }

  @Test
  public void testPojoListDeserialization() throws JsonDeserializationException {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();

    Object testClassList =
        jsonLibrary.deserializeAt(
            "[{\"foo\": \"one\"},{\"foo\": \"two\"},{\"foo\": \"three\"}]",
            new ListParameterizedType(TestClass.class));

    assertThat(
        (List<Object>) testClassList,
        hasItems(
            hasProperty("foo", equalTo("one")),
            hasProperty("foo", equalTo("two")),
            hasProperty("foo", equalTo("three"))));
  }

  @Test
  public void testStringListDeserialization() throws JsonDeserializationException {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();

    Object testClass =
        jsonLibrary.deserializeAt(
            "[\"foo\", \"foo\", \"foo\"]", new ListParameterizedType(String.class));

    assertThat((List<Object>) testClass, hasItems(equalTo("foo"), equalTo("foo"), equalTo("foo")));
  }

  @Test
  public void testSerialization() throws Exception {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();

    TestClass testClass = new TestClass("baar");
    assertThat(jsonLibrary.serialize(testClass), is(equalTo("{\"foo\":\"baar\"}")));
  }

  @Test(expected = JsonSerializationException.class)
  public void testFailingSerialization() throws Exception {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    jsonLibrary.serialize(new TestClassThatFailsSerialization());
  }

  @Test
  public void testMustBeUsedForNegative() {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    assertThat(jsonLibrary.mustBeUsedFor(TestClass.class), is(false));
  }

  @Test
  public void testMustBeUsedForPositive() {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    assertThat(jsonLibrary.mustBeUsedFor(TestClassWithGsonAnnotations.class), is(true));
  }

  @Test
  public void testMustBeUsedForListNegative() {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    assertThat(jsonLibrary.mustBeUsedFor(new ListParameterizedType(TestClass.class)), is(false));
  }

  @Test
  public void testMustBeUsedForListPositive() {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    assertThat(
        jsonLibrary.mustBeUsedFor(new ListParameterizedType(TestClassWithGsonAnnotations.class)),
        is(true));
  }

  public static class TestClass {
    private final String foo;

    public TestClass(String foo) {
      this.foo = foo;
    }

    public String getFoo() {
      return foo;
    }
  }

  public static class TestClassWithGsonAnnotations {
    @BogusAnnotation
    @SerializedName("bookingId")
    private final String __internal_name;

    public TestClassWithGsonAnnotations(String internal) {
      this.__internal_name = internal;
    }

    public String getBookingId() {
      return __internal_name;
    }
  }

  public static class TestClassThatFailsSerialization {
    @JsonAdapter(FailingJsonSerializer.class)
    public String field = "value";
  }

  public static class FailingJsonSerializer implements JsonSerializer<String> {
    @Override
    public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
      throw new JsonSyntaxException("This JsonSerializer will always fail!");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface BogusAnnotation {}
}
