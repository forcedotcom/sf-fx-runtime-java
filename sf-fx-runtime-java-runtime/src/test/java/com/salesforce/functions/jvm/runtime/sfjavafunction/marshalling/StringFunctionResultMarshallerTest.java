/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionResultMarshallingException;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;

public class StringFunctionResultMarshallerTest {

  @Test
  public void testSuccess() {
    FunctionResultMarshaller marshaller = new StringFunctionResultMarshaller();
    String data = "Hello 👋🏻!";
    SalesforceFunctionResult result = marshaller.marshall(data);
    Assert.assertEquals(MediaType.PLAIN_TEXT_UTF_8, result.getMediaType());
    Assert.assertArrayEquals(data.getBytes(StandardCharsets.UTF_8), result.getData());
  }

  @Test(expected = FunctionResultMarshallingException.class)
  public void testFailure() {
    FunctionResultMarshaller marshaller = new StringFunctionResultMarshaller();
    marshaller.marshall(23);
  }
}
