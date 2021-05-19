/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;

public class PayloadUnmarshallers {
  public static PayloadUnmarshaller forTypeString(String typeString, ClassLoader classLoader)
      throws ClassNotFoundException, AmbiguousJsonLibraryException {

    switch (typeString) {
      case "byte[]":
        return new ByteArrayPayloadUnmarshaller();
      default:
        return new PojoFromJsonPayloadUnmarshaller(classLoader.loadClass(typeString));
    }
  }

  private PayloadUnmarshallers() {}
}
