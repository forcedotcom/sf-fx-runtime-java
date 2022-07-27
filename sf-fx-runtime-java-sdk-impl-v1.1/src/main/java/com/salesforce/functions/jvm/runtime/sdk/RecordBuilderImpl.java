/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.ReferenceId;
import com.salesforce.functions.jvm.sdk.data.builder.RecordBuilder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RecordBuilderImpl extends AbstractRecordAccessorImpl implements RecordBuilder {
  private final TreeMap<String, JsonElement> fieldValues =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  public RecordBuilderImpl(String type) {
    super(type);
  }

  public RecordBuilderImpl(String type, Map<String, JsonElement> fieldValues) {
    super(type);
    this.fieldValues.putAll(fieldValues);
  }

  @Nonnull
  @Override
  public Record build() {
    return new RecordImpl(getType(), fieldValues);
  }

  @Nonnull
  @Override
  public RecordBuilder withoutField(String name) {
    fieldValues.remove(name);
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withNullField(String name) {
    fieldValues.put(name, JsonNull.INSTANCE);
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, @Nullable String value) {
    if (value == null) {
      return withNullField(name);
    }

    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, short value) {
    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, long value) {
    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, int value) {
    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, float value) {
    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, double value) {
    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, byte value) {
    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, boolean value) {
    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, @Nullable BigInteger value) {
    if (value == null) {
      return withNullField(name);
    }

    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, @Nullable BigDecimal value) {
    if (value == null) {
      return withNullField(name);
    }

    fieldValues.put(name, new JsonPrimitive(value));
    return this;
  }

  @Nonnull
  @Override
  public RecordBuilder withField(String name, @Nullable ReferenceId value) {
    if (value == null) {
      return withNullField(name);
    }

    if (!(value instanceof ReferenceIdImpl)) {
      throw new IllegalArgumentException(
          "Given ReferenceId is not compatible with this RecordBuilder instance!");
    }

    ReferenceIdImpl referenceIdImpl = (ReferenceIdImpl) value;

    fieldValues.put(name, new JsonPrimitive(referenceIdImpl.toApiString()));
    return this;
  }

  @Override
  protected TreeMap<String, JsonElement> getFieldValues() {
    return fieldValues;
  }
}
