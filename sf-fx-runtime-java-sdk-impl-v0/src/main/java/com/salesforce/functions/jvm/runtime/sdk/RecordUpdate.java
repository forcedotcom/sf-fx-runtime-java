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

public class RecordUpdate
    extends AbstractRecordModification<com.salesforce.functions.jvm.sdk.data.RecordUpdate>
    implements com.salesforce.functions.jvm.sdk.data.RecordUpdate {
  private final String type;
  private final String id;

  public RecordUpdate(String type, String id, Map<String, JsonPrimitive> values) {
    super(values);
    this.type = type;
    this.id = id;
  }

  @Override
  protected com.salesforce.functions.jvm.sdk.data.RecordUpdate copy(
      Map<String, JsonPrimitive> values) {
    return new RecordUpdate(type, id, values);
  }

  @Override
  @Nonnull
  public String getType() {
    return type;
  }

  @Override
  @Nonnull
  public String getId() {
    return id;
  }
}
