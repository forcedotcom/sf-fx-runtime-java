/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Constants {
  public static String DEFAULT_SALESFORCE_API_VERSION = "53.0";

  public static final List<String> SUPPORTED_SALESFORCE_API_VERSIONS;

  static {
    List<String> versions = new ArrayList<>();
    versions.add("53.0");
    versions.add("54.0");
    versions.add("55.0");

    SUPPORTED_SALESFORCE_API_VERSIONS = Collections.unmodifiableList(versions);
  }
}
