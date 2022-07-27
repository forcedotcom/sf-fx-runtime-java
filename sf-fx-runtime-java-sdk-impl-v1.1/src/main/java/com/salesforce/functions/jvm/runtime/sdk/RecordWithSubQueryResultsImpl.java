/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonElement;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.RecordWithSubQueryResults;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;

public class RecordWithSubQueryResultsImpl extends RecordImpl implements RecordWithSubQueryResults {
  public <A extends JsonElement> RecordWithSubQueryResultsImpl(
      String type, Map<String, A> fieldValues) {
    super(type, fieldValues);
  }

  @Nonnull
  @Override
  public Optional<RecordQueryResult> getSubQueryResult(String objectName) {
    return Optional.empty();
  }
}
