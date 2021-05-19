/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.sdk;

import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionsInvocable;
import io.github.classgraph.TypeArgument;

public final class SdkFunction {
  private final String className;
  private final TypeArgument inputType;
  private final TypeArgument returnType;
  private final SalesforceFunctionsInvocable invocationWrapper;

  public SdkFunction(
      String className,
      TypeArgument inputType,
      TypeArgument returnType,
      SalesforceFunctionsInvocable invocationWrapper) {
    this.className = className;
    this.inputType = inputType;
    this.returnType = returnType;
    this.invocationWrapper = invocationWrapper;
  }

  public String getClassName() {
    return className;
  }

  public TypeArgument getInputType() {
    return inputType;
  }

  public TypeArgument getReturnType() {
    return returnType;
  }

  public SalesforceFunctionsInvocable getInvocationWrapper() {
    return invocationWrapper;
  }
}
