/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.util;

import java.util.Arrays;
import java.util.List;

public class StackTraceUtils {

  public static List<StackTraceElement> rebase(
      StackTraceElement[] stackTraceElements, String rootClass) {

    int lastRelevantIndex = stackTraceElements.length;
    for (int i = stackTraceElements.length; i > 0; i--) {
      StackTraceElement element = stackTraceElements[i - 1];

      if (element.getClassName().equals(rootClass)) {
        lastRelevantIndex = i;
        break;
      }
    }

    return Arrays.asList(Arrays.copyOfRange(stackTraceElements, 0, lastRelevantIndex));
  }

  private StackTraceUtils() {}
}
