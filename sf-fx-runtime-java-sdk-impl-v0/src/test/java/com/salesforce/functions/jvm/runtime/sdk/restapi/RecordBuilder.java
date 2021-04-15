/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.Map;

public final class RecordBuilder {
  public static Map<String, JsonPrimitive> map(Tuple... data) {
    HashMap<String, JsonPrimitive> result = new HashMap<>();
    for (Tuple tuple : data) {
      result.put(tuple.getKey(), tuple.getValue());
    }

    return result;
  }

  public static class Tuple {
    private final String key;
    private final JsonPrimitive value;

    public Tuple(String key, String value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public Tuple(String key, Number value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public Tuple(String key, Boolean value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public Tuple(String key, Character value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public String getKey() {
      return key;
    }

    public JsonPrimitive getValue() {
      return value;
    }
  }
}
