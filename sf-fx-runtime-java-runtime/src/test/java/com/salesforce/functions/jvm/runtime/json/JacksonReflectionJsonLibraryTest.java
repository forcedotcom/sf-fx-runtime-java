/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonSerializationException;
import java.util.List;
import org.junit.Test;

public class JacksonReflectionJsonLibraryTest {

  @Test
  public void testDeserialization() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    Object testClass = jsonLibrary.deserializeAt("{\"foo\": \"bar\"}", TestClass.class);

    assertThat(((TestClass) testClass).getFoo(), is(equalTo("bar")));
  }

  @Test
  public void testDeserializationWithPath() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    Object testClass =
        jsonLibrary.deserializeAt("{\"inner\": {\"foo\": \"bar\"}}", TestClass.class, "inner");
    assertThat(((TestClass) testClass).getFoo(), is(equalTo("bar")));
  }

  @Test
  public void testPojoListDeserialization() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());

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
  public void testStringListDeserialization() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());

    Object testClass =
        jsonLibrary.deserializeAt(
            "[\"foo\", \"foo\", \"foo\"]", new ListParameterizedType(String.class));

    assertThat((List<Object>) testClass, hasItems(equalTo("foo"), equalTo("foo"), equalTo("foo")));
  }

  @Test
  public void testSerialization() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());

    TestClass testClass = new TestClass("baar");
    assertThat(jsonLibrary.serialize(testClass), is(equalTo("{\"foo\":\"baar\"}")));
  }

  @Test(expected = JsonDeserializationException.class)
  public void testDeserializationExceptionWrapping() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    jsonLibrary.deserializeAt("{\"foo: \"bar\"}", Test.class);
  }

  @Test(expected = JsonSerializationException.class)
  public void testSerializationExceptionWrapping() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    jsonLibrary.serialize(new TestClassThatFailsSerialization());
  }

  @Test
  public void testMustBeUsedForNegative() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    assertThat(jsonLibrary.mustBeUsedFor(TestClass.class), is(false));
  }

  @Test
  public void testMustBeUsedForPositive() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    assertThat(jsonLibrary.mustBeUsedFor(TestClassThatFailsSerialization.class), is(true));
  }

  @Test
  public void testMustBeUsedForListNegative() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    assertThat(jsonLibrary.mustBeUsedFor(new ListParameterizedType(TestClass.class)), is(false));
  }

  @Test
  public void testMustBeUsedForListPositive() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    assertThat(
        jsonLibrary.mustBeUsedFor(new ListParameterizedType(TestClassThatFailsSerialization.class)),
        is(true));
  }

  public static class TestClass {
    private String foo;

    public TestClass() {}

    public TestClass(String foo) {
      this.foo = foo;
    }

    public String getFoo() {
      return foo;
    }
  }

  public static class TestClassThatFailsSerialization {
    @JsonSerialize(using = FailingJsonSerializer.class)
    public String field = "value";
  }

  public static class FailingJsonSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(
        String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
      throw new RuntimeException("This JsonSerializer will always fail!");
    }
  }
}
