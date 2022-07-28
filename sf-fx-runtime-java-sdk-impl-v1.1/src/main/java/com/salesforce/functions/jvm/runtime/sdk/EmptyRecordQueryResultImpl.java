/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.RecordWithSubQueryResults;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class EmptyRecordQueryResultImpl implements RecordQueryResult {
  private final boolean done;
  private final long totalSize;

  public EmptyRecordQueryResultImpl(boolean done, long totalSize) {
    this.done = done;
    this.totalSize = totalSize;
  }

  @Override
  public boolean isDone() {
    return done;
  }

  @Override
  public long getTotalSize() {
    return totalSize;
  }

  @Nonnull
  @Override
  public List<RecordWithSubQueryResults> getRecords() {
    return Collections.emptyList();
  }
}
