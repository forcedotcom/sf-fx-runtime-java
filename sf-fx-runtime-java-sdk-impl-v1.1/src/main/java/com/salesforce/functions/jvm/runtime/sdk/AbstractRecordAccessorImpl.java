/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.RecordAccessor;
import com.salesforce.functions.jvm.sdk.data.error.FieldConversionException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nonnull;

public abstract class AbstractRecordAccessorImpl implements RecordAccessor {
  private final String type;

  protected AbstractRecordAccessorImpl(String type) {
    this.type = type;
  }

  protected abstract TreeMap<String, FieldValue> getFieldValues();

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
        .flatMap(fieldValue -> Optional.ofNullable(fieldValue.getJsonData()))
        .map(JsonElement::isJsonNull)
        .orElse(false);
  }

  @Nonnull
  @Override
  public Optional<String> getStringField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsString, String.class);
  }

  @Nonnull
  @Override
  public Optional<Boolean> getBooleanField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsBoolean, Boolean.class);
  }

  @Nonnull
  @Override
  public Optional<Byte> getByteField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsByte, Byte.class);
  }

  @Nonnull
  @Override
  public Optional<Short> getShortField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsShort, Short.class);
  }

  @Nonnull
  @Override
  public Optional<Integer> getIntField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsInt, Integer.class);
  }

  @Nonnull
  @Override
  public Optional<Long> getLongField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsLong, Long.class);
  }

  @Nonnull
  @Override
  public Optional<Float> getFloatField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsFloat, Float.class);
  }

  @Nonnull
  @Override
  public Optional<Double> getDoubleField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsDouble, Double.class);
  }

  @Nonnull
  @Override
  public Optional<BigInteger> getBigIntegerField(String name) {
    return getBigDecimalField(name).map(BigDecimal::toBigInteger);
  }

  @Nonnull
  @Override
  public Optional<BigDecimal> getBigDecimalField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsBigDecimal, BigDecimal.class);
  }

  @Nonnull
  @Override
  public Optional<ByteBuffer> getBinaryField(String name) {
    Optional<FieldValue> fieldValue =
        Optional.ofNullable(getFieldValues().get(name))
            .filter(value -> !value.isJsonData() || !value.getJsonData().isJsonNull());

    if (fieldValue.isPresent() && !fieldValue.get().isBinaryData()) {
      throw new FieldConversionException(
          String.format("Field %s cannot be converted to ByteBuffer.", name));
    } else {
      return fieldValue.flatMap(value -> Optional.ofNullable(value.getBinaryData()));
    }
  }

  @Nonnull
  @Override
  public Optional<Record> getRecordField(String name) {
    FieldValue value = getFieldValues().get(name);

    if (isNullField(name) || value == null) {
      return Optional.empty();
    }

    if (!value.isRecordData()) {
      throw new FieldConversionException(
          String.format("Field %s cannot be converted to Record.", name));
    }

    return Optional.ofNullable(value.getRecordData());
  }

  @Nonnull
  private <T> Optional<T> getJsonFieldValue(
      String fieldName, Function<JsonPrimitive, T> f, Class<T> klass)
      throws FieldConversionException {
    Optional<FieldValue> fieldValue = Optional.ofNullable(getFieldValues().get(fieldName));

    if (fieldValue.isPresent() && fieldValue.get().isBinaryData()) {
      throw new FieldConversionException(
          "Binary data fields cannot be converted to a " + klass.getName() + "!");
    } else if (fieldValue.isPresent() && fieldValue.get().isRecordData()) {
      throw new FieldConversionException(
          "Record data fields cannot be converted to a " + klass.getName() + "!");
    } else {
      try {
        return fieldValue
            .flatMap(v -> Optional.ofNullable(v.getJsonData()))
            .filter(JsonElement::isJsonPrimitive)
            .map(jsonElement -> f.apply(jsonElement.getAsJsonPrimitive()));
      } catch (NumberFormatException e) {
        throw new FieldConversionException("NumberFormatException while converting field!", e);
      }
    }
  }
}
