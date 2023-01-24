/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.csv.CsvTable;
import java.util.Optional;
import javax.annotation.Nullable;

public class BulkQueryResults {
  private final String locator;
  private final CsvTable table;

  public BulkQueryResults(@Nullable String locator, CsvTable table) {
    this.locator = locator;
    this.table = table;
  }

  public Optional<String> getLocator() {
    return Optional.ofNullable(locator);
  }

  public CsvTable getTable() {
    return table;
  }
}
