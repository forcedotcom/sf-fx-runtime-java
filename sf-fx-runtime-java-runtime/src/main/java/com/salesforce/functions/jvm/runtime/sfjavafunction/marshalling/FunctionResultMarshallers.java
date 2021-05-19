/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;

public class FunctionResultMarshallers {
  public static FunctionResultMarshaller forTypeString(String typeString, ClassLoader classLoader)
      throws ClassNotFoundException, AmbiguousJsonLibraryException {

    switch (typeString) {
      case "java.lang.String":
        return new StringFunctionResultMarshaller();
      default:
        return new PojoAsJsonFunctionResultMarshaller(classLoader.loadClass(typeString));
    }
  }

  private FunctionResultMarshallers() {}
}
