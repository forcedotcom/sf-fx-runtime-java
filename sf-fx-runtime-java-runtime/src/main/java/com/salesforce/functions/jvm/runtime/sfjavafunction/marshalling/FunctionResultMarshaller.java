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
import java.lang.reflect.Type;

public interface FunctionResultMarshaller {
  MediaType getMediaType();

  Type getSourceType();

  byte[] marshallBytes(Object object) throws FunctionResultMarshallingException;

  default SalesforceFunctionResult marshall(Object object)
      throws FunctionResultMarshallingException {
    return new SalesforceFunctionResult(getMediaType(), marshallBytes(object));
  }
}
