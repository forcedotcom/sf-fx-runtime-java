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
  public static Map<String, JsonPrimitive> jsonPrimitiveMap(JsonPrimitiveTuple... data) {
    HashMap<String, JsonPrimitive> result = new HashMap<>();
    for (JsonPrimitiveTuple tuple : data) {
      result.put(tuple.getKey(), tuple.getValue());
    }

    return result;
  }

  public static Map<String, QueryRecordResult> queryResultMap(QueryResultTuple... data) {
    HashMap<String, QueryRecordResult> result = new HashMap<>();
    for (QueryResultTuple tuple : data) {
      result.put(tuple.getKey(), tuple.getValue());
    }

    return result;
  }

  public static class QueryResultTuple {
    private final String key;
    private final QueryRecordResult value;

    public QueryResultTuple(String key, QueryRecordResult value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public QueryRecordResult getValue() {
      return value;
    }
  }

  public static class JsonPrimitiveTuple {
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
}
