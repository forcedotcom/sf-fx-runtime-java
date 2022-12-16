/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.Map;

public final class RecordBuilder {
  public static Map<String, JsonPrimitive> attributes(JsonPrimitiveTuple... data) {
    HashMap<String, JsonPrimitive> result = new HashMap<>();
    for (JsonPrimitiveTuple tuple : data) {
      result.put(tuple.getKey(), tuple.getValue());
    }

    return result;
  }

  public static JsonPrimitiveTuple attribute(String name, String value) {
    return new JsonPrimitiveTuple(name, value);
  }

  public static Map<String, Record.FieldValue> fields(FieldValueTuple... data) {
    HashMap<String, Record.FieldValue> result = new HashMap<>();
    for (FieldValueTuple tuple : data) {
      result.put(tuple.getKey(), tuple.getValue());
    }
    return result;
  }

  public static FieldValueTuple field(String name, String value) {
    return new FieldValueTuple(name, new Record.FieldValue(new JsonPrimitive(value)));
  }

  public static FieldValueTuple field(String name, QueryRecordResult queryRecordResult) {
    return new FieldValueTuple(name, new Record.FieldValue(queryRecordResult));
  }

  static class JsonPrimitiveTuple {
    private final String key;
    private final JsonPrimitive value;

    public JsonPrimitiveTuple(String key, String value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public JsonPrimitiveTuple(String key, Number value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public JsonPrimitiveTuple(String key, Boolean value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public JsonPrimitiveTuple(String key, Character value) {
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

  static class FieldValueTuple {
    private final String key;
    private final Record.FieldValue value;

    public FieldValueTuple(String key, Record.FieldValue value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public Record.FieldValue getValue() {
      return value;
    }
  }
}
