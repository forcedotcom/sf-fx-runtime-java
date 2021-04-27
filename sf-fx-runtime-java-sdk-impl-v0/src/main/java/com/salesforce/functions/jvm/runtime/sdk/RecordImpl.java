/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonElement;
import com.salesforce.functions.jvm.sdk.data.Record;
import java.util.Map;
import java.util.TreeMap;

public class RecordImpl extends AbstractRecordAccessorImpl implements Record {
  private final TreeMap<String, JsonElement> fieldValues =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  public <A extends JsonElement> RecordImpl(String type, Map<String, A> fieldValues) {
    super(type);
    this.fieldValues.putAll(fieldValues);
  }

  @Override
  public TreeMap<String, JsonElement> getFieldValues() {
    return fieldValues;
  }
}
