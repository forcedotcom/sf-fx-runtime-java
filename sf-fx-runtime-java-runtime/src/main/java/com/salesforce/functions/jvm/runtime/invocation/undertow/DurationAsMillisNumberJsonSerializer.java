/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.invocation.undertow;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.Duration;

public class DurationAsMillisNumberJsonSerializer implements JsonSerializer<Duration> {
  @Override
  public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.toMillis());
  }
}
