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

public class PayloadUnmarshallersTest {
  @Test
  public void testByteArray() throws AmbiguousJsonLibraryException, ClassNotFoundException {
    PayloadUnmarshaller unmarshaller =
        PayloadUnmarshallers.forTypeString("byte[]", getClass().getClassLoader());

    assertThat(unmarshaller, is(instanceOf(ByteArrayPayloadUnmarshaller.class)));
  }

  @Test
  public void testPojo() throws AmbiguousJsonLibraryException, ClassNotFoundException {
    PayloadUnmarshaller unmarshaller =
        PayloadUnmarshallers.forTypeString(
            "com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.PayloadUnmarshallersTest$TestPojo",
            getClass().getClassLoader());

    assertThat(unmarshaller, is(instanceOf(PojoFromJsonPayloadUnmarshaller.class)));
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
