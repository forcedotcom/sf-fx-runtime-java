/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CsvTable {
  private final List<String> headers;
  private final List<List<String>> rows;

  public CsvTable(List<String> headers, List<List<String>> rows) {
    this.headers = Collections.unmodifiableList(new ArrayList<>(headers));

    List<List<String>> copiedRows = new ArrayList<>();
    for (List<String> row : rows) {
      List<String> copiedRow = new ArrayList<>();

      for (int i = 0; i < headers.size(); i++) {
        if (i < row.size()) {
          copiedRow.add(row.get(i));
        } else {
          copiedRow.add(null);
        }
      }

      copiedRows.add(Collections.unmodifiableList(copiedRow));
    }

    this.rows = Collections.unmodifiableList(copiedRows);
  }

  public List<String> getHeaders() {
    return headers;
  }

  public List<List<String>> getRows() {
    return rows;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CsvTable csvTable = (CsvTable) o;
    return Objects.equals(headers, csvTable.headers) && Objects.equals(rows, csvTable.rows);
  }

  @Override
  public int hashCode() {
    return Objects.hash(headers, rows);
  }

  @Override
  public String toString() {
    return "CsvTable{" + "headers=" + headers + ", rows=" + rows + '}';
  }
}
