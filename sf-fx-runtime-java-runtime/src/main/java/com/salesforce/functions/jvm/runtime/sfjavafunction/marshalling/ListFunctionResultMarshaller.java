/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionResultMarshallingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ListFunctionResultMarshaller implements FunctionResultMarshaller {
  @Override
  public MediaType getMediaType() {
    return MediaType.JSON_UTF_8;
  }

  @Override
  public Class<?> getSourceClass() {
    return List.class;
  }

  @Override
  public byte[] marshallBytes(Object object) throws FunctionResultMarshallingException {
    if (!(object instanceof List)) {
      throw new FunctionResultMarshallingException(
          String.format(
              "Expected java.lang.List for marshalling, got %s!", object.getClass().getName()));
    }

    return new Gson().toJson((List<String>) object, List.class).getBytes(StandardCharsets.UTF_8);
  }
}
