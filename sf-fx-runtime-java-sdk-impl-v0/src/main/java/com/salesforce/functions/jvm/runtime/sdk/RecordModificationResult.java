/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.ModifyRecordResult;

public class RecordModificationResult
    implements com.salesforce.functions.jvm.sdk.data.RecordModificationResult {
  private final ModifyRecordResult result;

  public RecordModificationResult(ModifyRecordResult result) {
    this.result = result;
  }

  @Override
  public String getId() {
    return result.getId();
  }
}
