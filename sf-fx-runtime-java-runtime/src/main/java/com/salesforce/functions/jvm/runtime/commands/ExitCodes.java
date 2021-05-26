/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

final class ExitCodes {
  public static final int SUCCESS = 0;
  public static final int NO_FUNCTIONS_FOUND = 1;
  public static final int MULTIPLE_FUNCTIONS_FOUND = 2;
  public static final int CANNOT_WRITE_BUNDLE = 3;
  public static final int BUNDLE_DIRECTORY_NOT_EMPTY = 4;
  public static final int BUNDLE_DIRECTORY_NOT_A_DIRECTORY = 5;
  public static final int UNEXPECTED_FILE_TYPE = 6;
  public static final int NO_PROJECT_FOUND = 7;
  public static final int MISSING_OR_INVALID_ARGUMENTS = 250;

  private ExitCodes() {}
}
