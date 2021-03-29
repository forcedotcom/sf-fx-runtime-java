/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.exception;

public class PayloadUnmarshallingException extends SalesforceFunctionException {
  public PayloadUnmarshallingException(String message) {
    super(message);
  }

  public PayloadUnmarshallingException(String message, Throwable cause) {
    super(message, cause);
  }

  public PayloadUnmarshallingException(Throwable cause) {
    super(cause);
  }
}
