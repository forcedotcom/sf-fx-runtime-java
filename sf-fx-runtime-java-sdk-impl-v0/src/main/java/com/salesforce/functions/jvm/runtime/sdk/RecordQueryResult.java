/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.QueryRecordResult;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordQueryResult implements com.salesforce.functions.jvm.sdk.data.RecordQueryResult {
  private final QueryRecordResult queryRecordResult;

  public RecordQueryResult(QueryRecordResult queryRecordResult) {
    this.queryRecordResult = queryRecordResult;
  }

  @Override
  public boolean isDone() {
    return queryRecordResult.isDone();
  }

  @Override
  public long getTotalSize() {
    return queryRecordResult.getTotalSize();
  }

  @Override
  public List<com.salesforce.functions.jvm.sdk.data.Record> getRecords() {
    return queryRecordResult.getRecords().stream().map(Record::new).collect(Collectors.toList());
  }

  public Optional<String> getNextRecordsPath() {
    return queryRecordResult.getNextRecordsPath();
  }
}
