/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.JsonPrimitive;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class Record {
  private final Map<String, JsonPrimitive> attributes;
  private final Map<String, JsonPrimitive> values;
  private final Map<String, QueryRecordResult> subQueryResults;

  public Record(
      Map<String, JsonPrimitive> attributes,
      Map<String, JsonPrimitive> values,
      Map<String, QueryRecordResult> subQueryResults) {
    this.attributes = attributes;
    this.values = values;
    this.subQueryResults = subQueryResults;
  }

  @Nonnull
  public Map<String, JsonPrimitive> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  @Nonnull
  public Map<String, JsonPrimitive> getValues() {
    return Collections.unmodifiableMap(values);
  }

  @Nonnull
  public Map<String, QueryRecordResult> getSubQueryResults() {
    return Collections.unmodifiableMap(subQueryResults);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Record record = (Record) o;
    return Objects.equals(attributes, record.attributes)
        && Objects.equals(values, record.values)
        && Objects.equals(subQueryResults, record.subQueryResults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributes, values, subQueryResults);
  }

  @Override
  public String toString() {
    return "Record{"
        + "attributes="
        + attributes
        + ", values="
        + values
        + ", subQueryResults="
        + subQueryResults
        + '}';
  }
}
