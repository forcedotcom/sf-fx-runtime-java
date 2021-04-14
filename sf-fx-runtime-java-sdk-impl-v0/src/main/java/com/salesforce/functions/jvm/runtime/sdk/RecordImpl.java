/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonPrimitive;
import com.salesforce.functions.jvm.sdk.data.Record;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class RecordImpl implements Record {
  private final com.salesforce.functions.jvm.runtime.sdk.restapi.Record innerRecord;

  public RecordImpl(com.salesforce.functions.jvm.runtime.sdk.restapi.Record innerRecord) {
    this.innerRecord = innerRecord;
  }

  @Override
  @Nonnull
  public String getType() {
    return innerRecord.getAttributes().get("type").getAsString();
  }

  @Override
  @Nonnull
  public Optional<String> getStringValue(String key) {
    return getValue(key, JsonPrimitive::getAsString);
  }

  @Override
  @Nonnull
  public Optional<Boolean> getBooleanValue(String key) {
    return getValue(key, JsonPrimitive::getAsBoolean);
  }

  @Override
  @Nonnull
  public Optional<Integer> getIntValue(String key) {
    return getValue(key, JsonPrimitive::getAsInt);
  }

  @Override
  @Nonnull
  public Optional<Long> getLongValue(String key) {
    return getValue(key, JsonPrimitive::getAsLong);
  }

  @Override
  @Nonnull
  public Optional<Float> getFloatValue(String key) {
    return getValue(key, JsonPrimitive::getAsFloat);
  }

  @Override
  @Nonnull
  public Optional<Double> getDoubleValue(String key) {
    return getValue(key, JsonPrimitive::getAsDouble);
  }

  @Override
  @Nonnull
  public Optional<Short> getShortValue(String key) {
    return getValue(key, JsonPrimitive::getAsShort);
  }

  @Override
  @Nonnull
  public Optional<Number> getNumberValue(String key) {
    return getValue(key, JsonPrimitive::getAsNumber);
  }

  @Override
  @Nonnull
  public Optional<Character> getCharacterValue(String key) {
    return getValue(key, JsonPrimitive::getAsCharacter);
  }

  @Override
  @Nonnull
  public Optional<Byte> getByteValue(String key) {
    return getValue(key, JsonPrimitive::getAsByte);
  }

  @Override
  @Nonnull
  public Optional<BigInteger> getBigIntegerValue(String key) {
    return getValue(key, JsonPrimitive::getAsBigInteger);
  }

  @Override
  @Nonnull
  public Optional<BigDecimal> getBigDecimalValue(String key) {
    return getValue(key, JsonPrimitive::getAsBigDecimal);
  }

  private <T> Optional<T> getValue(String key, Function<JsonPrimitive, T> f) {
    return Optional.ofNullable(innerRecord.getValues().get(key)).map(f);
  }
}
