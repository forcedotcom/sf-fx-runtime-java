/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionResultMarshallingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ListFunctionResultMarshallerTest {

  @Test
  public void testSuccess() {
    FunctionResultMarshaller marshaller = new ListFunctionResultMarshaller();
    List<String> data = new ArrayList<>();
    data.add("Hello 👋🏻!");
    SalesforceFunctionResult result = marshaller.marshall(data);

    assertThat(result.getMediaType(), is(MediaType.JSON_UTF_8));

    assertThat(result.getData(), is(equalTo("[\"Hello 👋🏻!\"]".getBytes(StandardCharsets.UTF_8))));
  }

  @Test(expected = FunctionResultMarshallingException.class)
  public void testFailure() {
    FunctionResultMarshaller marshaller = new ListFunctionResultMarshaller();
    marshaller.marshall(23);
  }
}
