/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project;

import java.util.Optional;

public final class ProjectMetadata {
  private final String salesforceApiVersion;

  public ProjectMetadata(String salesforceApiVersion) {
    this.salesforceApiVersion = salesforceApiVersion;
  }

  public Optional<String> getSalesforceApiVersion() {
    return Optional.ofNullable(salesforceApiVersion);
  }
}
