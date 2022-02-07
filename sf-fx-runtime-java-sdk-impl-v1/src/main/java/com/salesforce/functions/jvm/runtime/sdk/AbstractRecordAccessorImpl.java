/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.salesforce.functions.jvm.sdk.data.RecordAccessor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import javax.annotation.Nonnull;

public abstract class AbstractRecordAccessorImpl implements RecordAccessor {
  private final String type;

  protected AbstractRecordAccessorImpl(String type) {
    this.type = type;
  }

  protected abstract TreeMap<String, JsonElement> getFieldValues();

  @Nonnull
  @Override
  public String getType() {
    return type;
  }

  @Nonnull
  @Override
  public Set<String> getFieldNames() {
    return getFieldValues().keySet();
  }

  @Override
  public boolean hasField(String name) {
    return getFieldValues().containsKey(name);
  }

  @Override
  public boolean isNullField(String name) {
    return Optional.ofNullable(getFieldValues().get(name))
        .map(JsonElement::isJsonNull)
        .orElse(false);
  }

  @Nonnull
  @Override
  public Optional<String> getStringField(String name) {
    return getFieldValue(name, JsonPrimitive::getAsString);
  }

  @Nonnull
  @Override
  public Optional<Boolean> getBooleanField(String name) {
    return getFieldValue(name, JsonPrimitive::getAsBoolean);
  }

  @Nonnull
  @Override
  public Optional<Byte> getByteField(String name) {
    return getFieldValue(name, JsonPrimitive::getAsByte);
  }

  @Nonnull
  @Override
  public Optional<Short> getShortField(String name) {
    return getFieldValue(name, JsonPrimitive::getAsShort);
  }

  @Nonnull
  @Override
  public Optional<Integer> getIntField(String name) {
    return getFieldValue(name, JsonPrimitive::getAsInt);
  }

  @Nonnull
  @Override
  public Optional<Long> getLongField(String name) {
    return getFieldValue(name, JsonPrimitive::getAsLong);
  }

  @Nonnull
  @Override
  public Optional<Float> getFloatField(String name) {
    return getFieldValue(name, JsonPrimitive::getAsFloat);
  }

  @Nonnull
  @Override
  public Optional<Double> getDoubleField(String name) {
    return getFieldValue(name, JsonPrimitive::getAsDouble);
  }

  @Nonnull
  @Override
  public Optional<BigInteger> getBigIntegerField(String name) {
    return getBigDecimalField(name).map(BigDecimal::toBigInteger);
  }

  @Nonnull
  @Override
  public Optional<BigDecimal> getBigDecimalField(String name) {
    return getFieldValue(name, JsonPrimitive::getAsBigDecimal);
  }

  @Nonnull
  private <T> Optional<T> getFieldValue(String fieldName, Function<JsonPrimitive, T> f) {
    return Optional.ofNullable(getFieldValues().get(fieldName))
        .filter(JsonElement::isJsonPrimitive)
        .map(jsonElement -> f.apply(jsonElement.getAsJsonPrimitive()));
  }
}
