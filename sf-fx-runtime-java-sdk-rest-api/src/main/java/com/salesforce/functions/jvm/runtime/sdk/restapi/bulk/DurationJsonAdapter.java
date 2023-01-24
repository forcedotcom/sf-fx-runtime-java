/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Duration;

public final class DurationJsonAdapter
    implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

  @Override
  public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return Duration.ofMillis(json.getAsLong());
  }

  @Override
  public JsonElement serialize(
      Duration duration, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(duration.toMillis());
  }
}
