/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.RecordWithSubQueryResults;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;

public class RecordWithSubQueryResultsImpl extends RecordImpl implements RecordWithSubQueryResults {
  private final Map<String, RecordQueryResultImpl> subQueryResults;

  public RecordWithSubQueryResultsImpl(
      String type,
      Map<String, FieldValue> fieldValues,
      Map<String, RecordQueryResultImpl> subQueryResults) {

    super(type, fieldValues);
    this.subQueryResults = subQueryResults;
  }

  @Nonnull
  @Override
  public Optional<RecordQueryResult> getSubQueryResult(String objectName) {
    return Optional.ofNullable(subQueryResults.get(objectName));
  }
}
