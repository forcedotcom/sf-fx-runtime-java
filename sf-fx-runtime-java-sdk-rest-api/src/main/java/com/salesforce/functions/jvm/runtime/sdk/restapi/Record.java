/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class Record {
  private final Map<String, JsonPrimitive> attributes;
  private final Map<String, FieldValue> values;

  public Record(Map<String, JsonPrimitive> attributes, Map<String, FieldValue> values) {
    this.attributes = attributes;
    this.values = values;
  }

  @Nonnull
  public Map<String, JsonPrimitive> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  @Nonnull
  public Map<String, FieldValue> getValues() {
    return Collections.unmodifiableMap(values);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Record record = (Record) o;
    return Objects.equals(attributes, record.attributes) && Objects.equals(values, record.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributes, values);
  }

  @Override
  public String toString() {
    return "Record{" + "attributes=" + attributes + ", values=" + values + '}';
  }

  public static class FieldValue {
    private final JsonElement jsonData;
    private final Record record;
    private final QueryRecordResult queryRecordResult;

    public FieldValue(@Nonnull JsonElement jsonData) {
      this.jsonData = jsonData;
      this.record = null;
      this.queryRecordResult = null;
    }

    public FieldValue(@Nonnull Record record) {
      this.record = record;
      this.jsonData = null;
      this.queryRecordResult = null;
    }

    public FieldValue(@Nonnull QueryRecordResult queryRecordResult) {
      this.queryRecordResult = queryRecordResult;
      this.jsonData = null;
      this.record = null;
    }

    public JsonElement getJsonData() {
      return jsonData;
    }

    public boolean isJsonData() {
      return this.jsonData != null;
    }

    public Record getRecordData() {
      return record;
    }

    public boolean isRecordData() {
      return record != null;
    }

    public QueryRecordResult getQueryRecordResult() {
      return queryRecordResult;
    }

    public boolean isQueryRecordResult() {
      return queryRecordResult != null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || !(o instanceof FieldValue)) return false;
      FieldValue that = (FieldValue) o;
      return Objects.equals(jsonData, that.jsonData)
          && Objects.equals(record, that.record)
          && Objects.equals(queryRecordResult, that.queryRecordResult);
    }

    @Override
    public int hashCode() {
      return Objects.hash(jsonData, record, queryRecordResult);
    }

    @Override
    public String toString() {
      return "FieldValue{"
          + "jsonData="
          + jsonData
          + ", record="
          + record
          + ", queryRecordResult="
          + queryRecordResult
          + '}';
    }
  }
}
