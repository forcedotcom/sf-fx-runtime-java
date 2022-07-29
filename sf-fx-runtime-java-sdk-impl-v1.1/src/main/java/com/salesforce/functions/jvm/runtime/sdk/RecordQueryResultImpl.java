/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.RecordWithSubQueryResults;
import java.util.*;
import javax.annotation.Nonnull;

public class RecordQueryResultImpl implements RecordQueryResult {
  private final boolean isDone;
  private final long totalSize;
  private final Optional<String> nextRecordsPath;
  private final List<RecordWithSubQueryResultsImpl> records;

  public RecordQueryResultImpl(
      boolean isDone,
      long totalSize,
      Optional<String> nextRecordsPath,
      List<RecordWithSubQueryResultsImpl> records) {
    this.isDone = isDone;
    this.totalSize = totalSize;
    this.nextRecordsPath = nextRecordsPath;
    this.records = records;
  }

  @Override
  public boolean isDone() {
    return isDone;
  }

  @Override
  public long getTotalSize() {
    return totalSize;
  }

  @Nonnull
  @Override
  public List<RecordWithSubQueryResults> getRecords() {
    return Collections.unmodifiableList(records);
  }

  public Optional<String> getNextRecordsPath() {
    return nextRecordsPath;
  }
}
