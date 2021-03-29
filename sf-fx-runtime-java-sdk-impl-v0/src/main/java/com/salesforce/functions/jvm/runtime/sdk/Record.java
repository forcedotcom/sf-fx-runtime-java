/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonPrimitive;
import com.salesforce.functions.jvm.runtime.sdk.restapi.QueryRecordResult;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;

public class Record implements com.salesforce.functions.jvm.sdk.data.Record {
  private final QueryRecordResult.Record innerRecord;

  public Record(QueryRecordResult.Record innerRecord) {
    this.innerRecord = innerRecord;
  }

  @Override
  public String getType() {
    return innerRecord.getAttributes().get("type").getAsString();
  }

  @Override
  public Optional<String> getStringValue(String key) {
    return getValue(key, JsonPrimitive::getAsString);
  }

  @Override
  public Optional<Boolean> getBooleanValue(String key) {
    return getValue(key, JsonPrimitive::getAsBoolean);
  }

  @Override
  public Optional<Integer> getIntValue(String key) {
    return getValue(key, JsonPrimitive::getAsInt);
  }

  @Override
  public Optional<Long> getLongValue(String key) {
    return getValue(key, JsonPrimitive::getAsLong);
  }

  @Override
  public Optional<Float> getFloatValue(String key) {
    return getValue(key, JsonPrimitive::getAsFloat);
  }

  @Override
  public Optional<Double> getDoubleValue(String key) {
    return getValue(key, JsonPrimitive::getAsDouble);
  }

  @Override
  public Optional<Short> getShortValue(String key) {
    return getValue(key, JsonPrimitive::getAsShort);
  }

  @Override
  public Optional<Number> getNumberValue(String key) {
    return getValue(key, JsonPrimitive::getAsNumber);
  }

  @Override
  public Optional<Character> getCharacterValue(String key) {
    return getValue(key, JsonPrimitive::getAsCharacter);
  }

  @Override
  public Optional<Byte> getByteValue(String key) {
    return getValue(key, JsonPrimitive::getAsByte);
  }

  @Override
  public Optional<BigInteger> getBigIntegerValue(String key) {
    return getValue(key, JsonPrimitive::getAsBigInteger);
  }

  @Override
  public Optional<BigDecimal> getBigDecimalValue(String key) {
    return getValue(key, JsonPrimitive::getAsBigDecimal);
  }

  private <T> Optional<T> getValue(String key, Function<JsonPrimitive, T> f) {
    return Optional.ofNullable(innerRecord.getValues().get(key)).map(f);
  }
}
