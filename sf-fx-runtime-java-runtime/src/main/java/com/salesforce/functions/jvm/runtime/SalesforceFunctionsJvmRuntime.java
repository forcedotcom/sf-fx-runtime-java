/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime;

import com.salesforce.functions.jvm.runtime.commands.MainCommand;
import picocli.CommandLine;

public final class SalesforceFunctionsJvmRuntime {
  public static void main(String[] args) {
    int exitCode = new CommandLine(new MainCommand()).execute(args);
    System.exit(exitCode);
  }

  private SalesforceFunctionsJvmRuntime() {}
}
