/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonPrimitive;
import java.util.Map;

public class RecordInsert
    extends AbstractRecordModification<com.salesforce.functions.jvm.sdk.data.RecordInsert>
    implements com.salesforce.functions.jvm.sdk.data.RecordInsert {
  private final String type;

  public RecordInsert(String type, Map<String, JsonPrimitive> values) {
    super(values);
    this.type = type;
  }

  @Override
  protected com.salesforce.functions.jvm.sdk.data.RecordInsert copy(
      Map<String, JsonPrimitive> values) {
    return new RecordInsert(type, values);
  }

  @Override
  public String getType() {
    return type;
  }
}
