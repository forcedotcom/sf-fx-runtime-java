/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.exception;

import java.util.List;

public class FunctionThrewExceptionException extends SalesforceFunctionException {
  private final List<StackTraceElement> functionStackTrace;

  public FunctionThrewExceptionException(
      Throwable cause, List<StackTraceElement> functionStackTrace) {

    super(cause);
    this.functionStackTrace = functionStackTrace;
  }

  public List<StackTraceElement> getFunctionStackTrace() {
    return functionStackTrace;
  }
}
