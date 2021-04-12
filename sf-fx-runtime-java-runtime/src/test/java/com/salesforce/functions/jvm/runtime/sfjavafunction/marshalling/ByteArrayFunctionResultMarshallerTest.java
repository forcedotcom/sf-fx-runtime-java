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
import org.junit.Assert;
import org.junit.Test;

public class ByteArrayFunctionResultMarshallerTest {

  @Test
  public void testSuccess() {
    FunctionResultMarshaller marshaller = new ByteArrayFunctionResultMarshaller();
    byte[] data = new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
    SalesforceFunctionResult result = marshaller.marshall(data);
    Assert.assertEquals(MediaType.OCTET_STREAM, result.getMediaType());
    Assert.assertEquals(data, result.getData());
  }

  @Test(expected = FunctionResultMarshallingException.class)
  public void testFailure() {
    FunctionResultMarshaller marshaller = new ByteArrayFunctionResultMarshaller();
    marshaller.marshall("Foobar");
  }
}
