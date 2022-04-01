/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import java.util.*;

public final class QueryRecordResult {
  private final long totalSize;
  private final boolean done;
  private final List<Record> records;
  private final String nextRecordsPath;

  public QueryRecordResult(
      long totalSize, boolean done, List<Record> records, String nextRecordsPath) {
    this.totalSize = totalSize;
    this.done = done;
    this.records = new ArrayList<>(records);
    this.nextRecordsPath = nextRecordsPath;
  }

  public List<Record> getRecords() {
    return Collections.unmodifiableList(records);
  }

  public long getTotalSize() {
    return totalSize;
  }

  public boolean isDone() {
    return done;
  }

  public Optional<String> getNextRecordsPath() {
    return Optional.ofNullable(nextRecordsPath);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryRecordResult result = (QueryRecordResult) o;
    return totalSize == result.totalSize
        && done == result.done
        && Objects.equals(records, result.records)
        && Objects.equals(nextRecordsPath, result.nextRecordsPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalSize, done, records, nextRecordsPath);
  }
}
