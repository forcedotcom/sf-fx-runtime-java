/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.sdk.data.Record;
import java.util.Map;
import java.util.TreeMap;

public class RecordImpl extends AbstractRecordAccessorImpl implements Record {
  private final TreeMap<String, FieldValue> fieldValues =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  public RecordImpl(String type, Map<String, FieldValue> fieldValues) {
    super(type);
    this.fieldValues.putAll(fieldValues);
  }

  @Override
  public TreeMap<String, FieldValue> getFieldValues() {
    return fieldValues;
  }
}
