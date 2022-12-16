/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonElement;
import com.salesforce.functions.jvm.sdk.data.Record;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

public class FieldValue {
  private final JsonElement jsonData;
  private final ByteBuffer binaryData;
  private final Record recordData;

  public FieldValue(@Nonnull JsonElement jsonData) {
    this.jsonData = jsonData;
    this.binaryData = null;
    this.recordData = null;
  }

  public FieldValue(@Nonnull ByteBuffer binaryData) {
    this.binaryData = binaryData;
    this.jsonData = null;
    this.recordData = null;
  }

  public FieldValue(@Nonnull Record recordData) {
    this.recordData = recordData;
    this.binaryData = null;
    this.jsonData = null;
  }

  public boolean isBinaryData() {
    return this.binaryData != null;
  }

  public boolean isJsonData() {
    return this.jsonData != null;
  }

  public boolean isRecordData() {
    return this.recordData != null;
  }

  public JsonElement getJsonData() {
    return jsonData;
  }

  public ByteBuffer getBinaryData() {
    return binaryData;
  }

  public Record getRecordData() {
    return this.recordData;
  }

  @Override
  public String toString() {
    return "FieldValue{"
        + "jsonData="
        + jsonData
        + ", binaryData="
        + binaryData
        + ", recordData="
        + recordData
        + '}';
  }
}
