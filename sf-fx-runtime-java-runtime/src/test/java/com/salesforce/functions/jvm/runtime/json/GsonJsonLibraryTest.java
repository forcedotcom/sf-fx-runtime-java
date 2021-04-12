/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import org.junit.Test;

public class GsonJsonLibraryTest {

  @Test
  public void testDeserialization() throws Exception {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    Object testClass = jsonLibrary.deserializeAt("{\"foo\": \"bar\"}", TestClass.class);
    assertEquals(((TestClass) testClass).getFoo(), "bar");
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
    assertEquals(((TestClass) testClass).getFoo(), "bar");
  }

  @Test
  public void testSerialization() throws Exception {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();

    TestClass testClass = new TestClass("baar");
    assertEquals("{\"foo\":\"baar\"}", jsonLibrary.serialize(testClass));
  }

  @Test
  public void testMustBeUsedForNegative() {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    assertFalse(jsonLibrary.mustBeUsedFor(TestClass.class));
  }

  @Test
  public void testMustBeUsedForPositive() {
    JsonLibrary jsonLibrary = new GsonJsonLibrary();
    assertTrue(jsonLibrary.mustBeUsedFor(TestClassWithGsonAnnotations.class));
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

  public static class TestClassWithGsonAnnotations {
    @SerializedName("bookingId")
    private final String __internal_name;

    public TestClassWithGsonAnnotations(String internal) {
      this.__internal_name = internal;
    }

    public String getBookingId() {
      return __internal_name;
    }
  }
}
