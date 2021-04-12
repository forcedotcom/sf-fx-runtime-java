/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonPrimitive;
import java.util.Map;
import javax.annotation.Nonnull;

public class RecordCreate
    extends AbstractRecordModification<com.salesforce.functions.jvm.sdk.data.RecordCreate>
    implements com.salesforce.functions.jvm.sdk.data.RecordCreate {
  private final String type;

  public RecordCreate(String type, Map<String, JsonPrimitive> values) {
    super(values);
    this.type = type;
  }

  @Override
  protected com.salesforce.functions.jvm.sdk.data.RecordCreate copy(
      Map<String, JsonPrimitive> values) {
    return new RecordCreate(type, values);
  }

  @Override
  @Nonnull
  public String getType() {
    return type;
  }
}
