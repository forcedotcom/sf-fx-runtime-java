/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.nio.charset.StandardCharsets;
import org.apache.http.entity.ContentType;

public class JsonRequestBody implements RestApiRequestBody {
  private final JsonElement jsonElement;

  public JsonRequestBody(JsonElement jsonElement) {
    this.jsonElement = jsonElement;
  }

  public JsonElement getJsonElement() {
    return jsonElement;
  }

  @Override
  public ContentType getContentType() {
    return ContentType.APPLICATION_JSON;
  }

  @Override
  public byte[] getRequestContents() {
    return new Gson().toJson(this.jsonElement).getBytes(StandardCharsets.UTF_8);
  }
}
