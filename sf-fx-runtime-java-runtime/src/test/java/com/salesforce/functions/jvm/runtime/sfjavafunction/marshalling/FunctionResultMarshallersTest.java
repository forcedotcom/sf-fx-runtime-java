/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import org.junit.Test;

public class FunctionResultMarshallersTest {
  @Test
  public void testJavaLangString() throws AmbiguousJsonLibraryException, ClassNotFoundException {
    FunctionResultMarshaller marshaller =
        FunctionResultMarshallers.forTypeString("java.lang.String", getClass().getClassLoader());

    assertThat(marshaller, is(instanceOf(StringFunctionResultMarshaller.class)));
  }

  @Test
  public void testPojo() throws AmbiguousJsonLibraryException, ClassNotFoundException {
    FunctionResultMarshaller marshaller =
        FunctionResultMarshallers.forTypeString(
            "com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.FunctionResultMarshallersTest$TestPojo",
            getClass().getClassLoader());

    assertThat(marshaller, is(instanceOf(PojoAsJsonFunctionResultMarshaller.class)));
  }

  static class TestPojo {
    private final String data;

    public TestPojo(String data) {
      this.data = data;
    }

    public String getData() {
      return data;
    }
  }
}
