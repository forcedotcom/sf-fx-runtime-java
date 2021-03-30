/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import static org.junit.Assert.assertEquals;

import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import org.junit.Test;

public class GsonReflectionJsonLibraryTest {

  @Test
  public void testDeserialization() throws Exception {
    JsonLibrary jsonLibrary = new GsonReflectionJsonLibrary(getClass().getClassLoader());
    Object testClass = jsonLibrary.deserializeAt("{\"foo\": \"bar\"}", TestClass.class);
    assertEquals(((TestClass) testClass).getFoo(), "bar");
  }

  @Test(expected = JsonDeserializationException.class)
  public void testExceptionWrapping() throws Exception {
    JsonLibrary jsonLibrary = new GsonReflectionJsonLibrary(getClass().getClassLoader());
    jsonLibrary.deserializeAt("{\"foo: \"bar\"}", Test.class);
  }

  @Test
  public void testDeserializationWithPath() throws Exception {
    JsonLibrary jsonLibrary = new GsonReflectionJsonLibrary(getClass().getClassLoader());
    Object testClass =
        jsonLibrary.deserializeAt("{\"inner\": {\"foo\": \"bar\"}}", TestClass.class, "inner");
    assertEquals(((TestClass) testClass).getFoo(), "bar");
  }

  @Test
  public void testSerialization() throws Exception {
    JsonLibrary jsonLibrary = new GsonReflectionJsonLibrary(getClass().getClassLoader());

    TestClass testClass = new TestClass("baar");
    assertEquals("{\"foo\":\"baar\"}", jsonLibrary.serialize(testClass));
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
}
