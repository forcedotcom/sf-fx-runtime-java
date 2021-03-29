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

public class JacksonReflectionJsonLibraryTest {

  @Test
  public void testDeserialization() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    Object testClass = jsonLibrary.deserializeAt("{\"foo\": \"bar\"}", TestClass.class);
    assertEquals(((TestClass) testClass).getFoo(), "bar");
  }

  @Test(expected = JsonDeserializationException.class)
  public void testExceptionWrapping() throws Exception {
    JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
    jsonLibrary.deserializeAt("{\"foo: \"bar\"}", Test.class);
  }

  public static class TestClass {
    public String foo;

    public String getFoo() {
      return foo;
    }
  }
}
