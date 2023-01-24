/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public final class SalesforceDateTimeStringJsonAdapter
    implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

  @Override
  public JsonElement serialize(Instant instant, Type srcType, JsonSerializationContext context) {
    return new JsonPrimitive(DATE_TIME_FORMATTER.format(instant));
  }

  @Override
  public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext context)
      throws JsonParseException {

    return Instant.from(DATE_TIME_FORMATTER.parse(json.getAsString()));
  }

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}
