/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.invocation.undertow;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;

public class StackTraceElementListJsonSerializer
    implements JsonSerializer<List<StackTraceElement>> {

  @Override
  public JsonElement serialize(
      List<StackTraceElement> src, Type typeOfSrc, JsonSerializationContext context) {

    JsonArray array = new JsonArray(src.size());
    for (StackTraceElement stackTraceElement : src) {
      array.add(stackTraceElement.toString());
    }

    return array;
  }
}
