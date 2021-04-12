/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonPrimitive;
import com.salesforce.functions.jvm.sdk.data.RecordCreate;
import java.util.Map;
import javax.annotation.Nonnull;

public class RecordCreateImpl extends AbstractRecordModification<RecordCreate>
    implements RecordCreate {
  private final String type;

  public RecordCreateImpl(String type, Map<String, JsonPrimitive> values) {
    super(values);
    this.type = type;
  }

  @Override
  protected RecordCreate copy(Map<String, JsonPrimitive> values) {
    return new RecordCreateImpl(type, values);
  }

  @Override
  @Nonnull
  public String getType() {
    return type;
  }
}
