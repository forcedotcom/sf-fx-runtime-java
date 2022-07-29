/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonElement;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

public class FieldValue {
  private final JsonElement jsonData;
  private final ByteBuffer binaryData;

  public FieldValue(@Nonnull JsonElement jsonData) {
    this.jsonData = jsonData;
    this.binaryData = null;
  }

  public FieldValue(@Nonnull ByteBuffer binaryData) {
    this.binaryData = binaryData;
    this.jsonData = null;
  }

  public boolean isBinaryData() {
    return this.binaryData != null;
  }

  public boolean isJsonData() {
    return this.jsonData != null;
  }

  public JsonElement getJsonData() {
    return jsonData;
  }

  public ByteBuffer getBinaryData() {
    return binaryData;
  }

  @Override
  public String toString() {
    return "FieldValue{" + "jsonData=" + jsonData + ", binaryData=" + binaryData + '}';
  }
}
