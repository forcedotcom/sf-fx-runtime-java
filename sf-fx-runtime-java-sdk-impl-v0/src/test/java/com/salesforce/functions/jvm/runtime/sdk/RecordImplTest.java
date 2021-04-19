/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonPrimitive;
import com.salesforce.functions.jvm.runtime.sdk.restapi.Record;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class RecordImplTest {
  private RecordImpl record;

  @Before
  public void setUp() {
    Map<String, JsonPrimitive> attributes = new HashMap<>();
    attributes.put("type", new JsonPrimitive("Movie__c"));

    Map<String, JsonPrimitive> values = new HashMap<>();
    values.put(NUMBER_MAX_BYTE_KEY, new JsonPrimitive(Byte.MAX_VALUE));
    values.put(NUMBER_MAX_SHORT_KEY, new JsonPrimitive(Short.MAX_VALUE));
    values.put(NUMBER_MAX_INT_KEY, new JsonPrimitive(Integer.MAX_VALUE));
    values.put(NUMBER_MAX_LONG_KEY, new JsonPrimitive(Long.MAX_VALUE));
    values.put(NUMBER_MAX_FLOAT_KEY, new JsonPrimitive(Float.MAX_VALUE));
    values.put(NUMBER_MAX_DOUBLE_KEY, new JsonPrimitive(Double.MAX_VALUE));
    values.put(NUMBER_BIG_INTEGER_KEY, new JsonPrimitive(BIG_INTEGER_TEST_VALUE));
    values.put(NUMBER_BIG_DECIMAL_KEY, new JsonPrimitive(BIG_DECIMAL_TEST_VALUE));
    values.put(STRING_ABC_KEY, new JsonPrimitive("abc"));
    values.put(STRING_EMPTY_KEY, new JsonPrimitive(""));
    values.put(STRING_TRUE_KEY, new JsonPrimitive("true"));
    values.put(STRING_TRUE_UPPERCASE_KEY, new JsonPrimitive("TRUE"));
    values.put(BOOLEAN_TRUE_KEY, new JsonPrimitive(true));
    values.put(BOOLEAN_FALSE_KEY, new JsonPrimitive(false));

    record = new RecordImpl(new Record(attributes, values));
  }

  @Test
  public void testGetType() {
    assertThat(record.getType(), is(equalTo("Movie__c")));
  }

  @Test
  public void testGetStringValue_MISSING_VALUE_KEY() {
    assertThat(record.getStringValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getStringValue(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo("" + Byte.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getStringValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo("" + Short.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getStringValue(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo("" + Integer.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getStringValue(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo("" + Long.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getStringValue(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo("" + Float.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getStringValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo("" + Double.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getStringValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo("18446744073709551614"))));
  }

  @Test
  public void testGetStringValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getStringValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo("3.5953862697246314E+308"))));
  }

  @Test
  public void testGetStringValue_STRING_ABC_KEY() {
    assertThat(record.getStringValue(STRING_ABC_KEY), is(optionalWithValue(equalTo("abc"))));
  }

  @Test
  public void testGetStringValue_STRING_EMPTY_KEY() {
    assertThat(record.getStringValue(STRING_EMPTY_KEY), is(optionalWithValue(equalTo(""))));
  }

  @Test
  public void testGetStringValue_STRING_TRUE_KEY() {
    assertThat(record.getStringValue(STRING_TRUE_KEY), is(optionalWithValue(equalTo("true"))));
  }

  @Test
  public void testGetStringValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThat(
        record.getStringValue(STRING_TRUE_UPPERCASE_KEY), is(optionalWithValue(equalTo("TRUE"))));
  }

  @Test
  public void testGetStringValue_BOOLEAN_TRUE_KEY() {
    assertThat(record.getStringValue(BOOLEAN_TRUE_KEY), is(optionalWithValue(equalTo("true"))));
  }

  @Test
  public void testGetStringValue_BOOLEAN_FALSE_KEY() {
    assertThat(record.getStringValue(BOOLEAN_FALSE_KEY), is(optionalWithValue(equalTo("false"))));
  }

  @Test
  public void testGetBooleanValue_MISSING_VALUE_KEY() {
    assertThat(record.getBooleanValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(record.getBooleanValue(NUMBER_MAX_BYTE_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(record.getBooleanValue(NUMBER_MAX_SHORT_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_INT_KEY() {
    assertThat(record.getBooleanValue(NUMBER_MAX_INT_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_LONG_KEY() {
    assertThat(record.getBooleanValue(NUMBER_MAX_LONG_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(record.getBooleanValue(NUMBER_MAX_FLOAT_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(record.getBooleanValue(NUMBER_MAX_DOUBLE_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_BIG_INT_KEY() {
    assertThat(record.getBooleanValue(NUMBER_BIG_INTEGER_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(record.getBooleanValue(NUMBER_BIG_DECIMAL_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_STRING_ABC_KEY() {
    assertThat(record.getBooleanValue(STRING_ABC_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_STRING_EMPTY_KEY() {
    assertThat(record.getBooleanValue(STRING_EMPTY_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_STRING_TRUE_KEY() {
    assertThat(record.getBooleanValue(STRING_TRUE_KEY), is(optionalWithValue(is(true))));
  }

  @Test
  public void testGetBooleanValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThat(record.getBooleanValue(STRING_TRUE_UPPERCASE_KEY), is(optionalWithValue(is(true))));
  }

  @Test
  public void testGetBooleanValue_BOOLEAN_TRUE_KEY() {
    assertThat(record.getBooleanValue(BOOLEAN_TRUE_KEY), is(optionalWithValue(is(true))));
  }

  @Test
  public void testGetBooleanValue_BOOLEAN_FALSE_KEY() {
    assertThat(record.getBooleanValue(BOOLEAN_FALSE_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetIntValue_MISSING_VALUE_KEY() {
    assertThat(record.getIntValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getIntValue(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((int) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getIntValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((int) Short.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getIntValue(NUMBER_MAX_INT_KEY), is(optionalWithValue(equalTo(Integer.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getIntValue(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((int) Long.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getIntValue(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((int) Float.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getIntValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((int) Double.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getIntValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.intValue()))));
  }

  @Test
  public void testGetIntValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getIntValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.intValue()))));
  }

  @Test(expected = NumberFormatException.class)
  public void testGetIntValue_STRING_ABC_KEY() {
    record.getIntValue(STRING_ABC_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetIntValue_STRING_EMPTY_KEY() {
    record.getIntValue(STRING_EMPTY_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetIntValue_STRING_TRUE_KEY() {
    record.getIntValue(STRING_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetIntValue_STRING_TRUE_UPPERCASE_KEY() {
    record.getIntValue(STRING_TRUE_UPPERCASE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetIntValue_BOOLEAN_TRUE_KEY() {
    record.getIntValue(BOOLEAN_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetIntValue_BOOLEAN_FALSE_KEY() {
    record.getIntValue(BOOLEAN_FALSE_KEY);
  }

  @Test
  public void testGetLongValue_MISSING_VALUE_KEY() {
    assertThat(record.getLongValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getLongValue(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((long) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getLongValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((long) Short.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getLongValue(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((long) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getLongValue(NUMBER_MAX_LONG_KEY), is(optionalWithValue(equalTo(Long.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getLongValue(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((long) Float.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getLongValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((long) Double.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getLongValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo((BIG_INTEGER_TEST_VALUE.longValue())))));
  }

  @Test
  public void testGetLongValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getLongValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.longValue()))));
  }

  @Test(expected = NumberFormatException.class)
  public void testGetLongValue_STRING_ABC_KEY() {
    record.getLongValue(STRING_ABC_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetLongValue_STRING_EMPTY_KEY() {
    record.getLongValue(STRING_EMPTY_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetLongValue_STRING_TRUE_KEY() {
    record.getLongValue(STRING_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetLongValue_STRING_TRUE_UPPERCASE_KEY() {
    record.getLongValue(STRING_TRUE_UPPERCASE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetLongValue_BOOLEAN_TRUE_KEY() {
    record.getLongValue(BOOLEAN_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetLongValue_BOOLEAN_FALSE_KEY() {
    record.getLongValue(BOOLEAN_FALSE_KEY);
  }

  @Test
  public void testGetFloatValue_MISSING_VALUE_KEY() {
    assertThat(record.getFloatValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getFloatValue(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((float) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getFloatValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((float) Short.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getFloatValue(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((float) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getFloatValue(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((float) Long.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getFloatValue(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((Float.MAX_VALUE)))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getFloatValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((float) Double.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getFloatValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.floatValue()))));
  }

  @Test
  public void testGetFloatValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getFloatValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.floatValue()))));
  }

  @Test(expected = NumberFormatException.class)
  public void testGetFloatValue_STRING_ABC_KEY() {
    record.getFloatValue(STRING_ABC_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetFloatValue_STRING_EMPTY_KEY() {
    record.getFloatValue(STRING_EMPTY_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetFloatValue_STRING_TRUE_KEY() {
    record.getFloatValue(STRING_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetFloatValue_STRING_TRUE_UPPERCASE_KEY() {
    record.getFloatValue(STRING_TRUE_UPPERCASE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetFloatValue_BOOLEAN_TRUE_KEY() {
    record.getFloatValue(BOOLEAN_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetFloatValue_BOOLEAN_FALSE_KEY() {
    record.getFloatValue(BOOLEAN_FALSE_KEY);
  }

  @Test
  public void testGetDoubleValue_MISSING_VALUE_KEY() {
    assertThat(record.getDoubleValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getDoubleValue(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((double) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getDoubleValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((double) Short.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getDoubleValue(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((double) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getDoubleValue(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((double) Long.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getDoubleValue(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((double) Float.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getDoubleValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo(Double.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getDoubleValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.doubleValue()))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getDoubleValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.doubleValue()))));
  }

  @Test(expected = NumberFormatException.class)
  public void testGetDoubleValue_STRING_ABC_KEY() {
    record.getDoubleValue(STRING_ABC_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetDoubleValue_STRING_EMPTY_KEY() {
    record.getDoubleValue(STRING_EMPTY_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetDoubleValue_STRING_TRUE_KEY() {
    record.getDoubleValue(STRING_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetDoubleValue_STRING_TRUE_UPPERCASE_KEY() {
    record.getDoubleValue(STRING_TRUE_UPPERCASE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetDoubleValue_BOOLEAN_TRUE_KEY() {
    record.getDoubleValue(BOOLEAN_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetDoubleValue_BOOLEAN_FALSE_KEY() {
    record.getDoubleValue(BOOLEAN_FALSE_KEY);
  }

  @Test
  public void testGetShortValue_MISSING_VALUE_KEY() {
    assertThat(record.getShortValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getShortValue(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((short) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getShortValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo(Short.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getShortValue(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((short) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getShortValue(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((short) Long.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getShortValue(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((short) Float.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getShortValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((short) Double.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getShortValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.shortValue()))));
  }

  @Test
  public void testGetShortValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getShortValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.shortValue()))));
  }

  @Test(expected = NumberFormatException.class)
  public void testGetShortValue_STRING_ABC_KEY() {
    record.getShortValue(STRING_ABC_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetShortValue_STRING_EMPTY_KEY() {
    record.getShortValue(STRING_EMPTY_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetShortValue_STRING_TRUE_KEY() {
    record.getShortValue(STRING_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetShortValue_STRING_TRUE_UPPERCASE_KEY() {
    record.getShortValue(STRING_TRUE_UPPERCASE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetShortValue_BOOLEAN_TRUE_KEY() {
    record.getShortValue(BOOLEAN_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetShortValue_BOOLEAN_FALSE_KEY() {
    record.getShortValue(BOOLEAN_FALSE_KEY);
  }

  @Test
  public void testGetCharacterValue_MISSING_VALUE_KEY() {
    assertThat(record.getCharacterValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetCharacterValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getCharacterValue(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo(String.valueOf(Byte.MAX_VALUE).charAt(0)))));
  }

  @Test
  public void testGetCharacterValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getCharacterValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo(String.valueOf(Short.MAX_VALUE).charAt(0)))));
  }

  @Test
  public void testGetCharacterValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getCharacterValue(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo(String.valueOf(Integer.MAX_VALUE).charAt(0)))));
  }

  @Test
  public void testGetCharacterValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getCharacterValue(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo(String.valueOf(Long.MAX_VALUE).charAt(0)))));
  }

  @Test
  public void testGetCharacterValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getCharacterValue(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo(String.valueOf(Float.MAX_VALUE).charAt(0)))));
  }

  @Test
  public void testGetCharacterValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getCharacterValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo(String.valueOf(Double.MAX_VALUE).charAt(0)))));
  }

  @Test
  public void testGetCharacterValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getCharacterValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.toString().charAt(0)))));
  }

  @Test
  public void testGetCharacterValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getCharacterValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.toString().charAt(0)))));
  }

  @Test
  public void testGetCharacterValue_STRING_ABC_KEY() {
    assertThat(record.getCharacterValue(STRING_ABC_KEY), is(optionalWithValue(equalTo('a'))));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetCharacterValue_STRING_EMPTY_KEY() {
    record.getCharacterValue(STRING_EMPTY_KEY);
  }

  @Test
  public void testGetCharacterValue_STRING_TRUE_KEY() {
    assertThat(record.getCharacterValue(STRING_TRUE_KEY), is(optionalWithValue(equalTo('t'))));
  }

  @Test
  public void testGetCharacterValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThat(
        record.getCharacterValue(STRING_TRUE_UPPERCASE_KEY), is(optionalWithValue(equalTo('T'))));
  }

  @Test
  public void testGetCharacterValue_BOOLEAN_TRUE_KEY() {
    assertThat(record.getCharacterValue(BOOLEAN_TRUE_KEY), is(optionalWithValue(equalTo('t'))));
  }

  @Test
  public void testGetCharacterValue_BOOLEAN_FALSE_KEY() {
    assertThat(record.getCharacterValue(BOOLEAN_FALSE_KEY), is(optionalWithValue(equalTo('f'))));
  }

  @Test
  public void testGetByteValue_MISSING_VALUE_KEY() {
    assertThat(record.getByteValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getByteValue(NUMBER_MAX_BYTE_KEY), is(optionalWithValue(equalTo(Byte.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getByteValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((byte) Short.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getByteValue(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((byte) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getByteValue(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((byte) Long.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getByteValue(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((byte) Float.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getByteValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((byte) Double.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getByteValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.byteValue()))));
  }

  @Test
  public void testGetByteValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getByteValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.byteValue()))));
  }

  @Test(expected = NumberFormatException.class)
  public void testGetByteValue_STRING_ABC_KEY() {
    record.getByteValue(STRING_ABC_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetByteValue_STRING_EMPTY_KEY() {
    record.getByteValue(STRING_EMPTY_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetByteValue_STRING_TRUE_KEY() {
    record.getByteValue(STRING_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetByteValue_STRING_TRUE_UPPERCASE_KEY() {
    record.getByteValue(STRING_TRUE_UPPERCASE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetByteValue_BOOLEAN_TRUE_KEY() {
    record.getByteValue(BOOLEAN_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetByteValue_BOOLEAN_FALSE_KEY() {
    record.getByteValue(BOOLEAN_FALSE_KEY);
  }

  @Test
  public void testGetBigIntegerValue_MISSING_VALUE_KEY() {
    assertThat(record.getBigIntegerValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getBigIntegerValue(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo(BigInteger.valueOf(Byte.MAX_VALUE)))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getBigIntegerValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo(BigInteger.valueOf(Short.MAX_VALUE)))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getBigIntegerValue(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo(BigInteger.valueOf(Integer.MAX_VALUE)))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getBigIntegerValue(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo(BigInteger.valueOf(Long.MAX_VALUE)))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getBigIntegerValue(NUMBER_MAX_FLOAT_KEY),
        // We cannot use BigDecimal.valueOf here, since it only accepts doubles, causing our
        // passed-in float to be promoted to double. This changes precision and no longer passes
        // the test.
        is(
            optionalWithValue(
                equalTo(new BigDecimal(String.valueOf(Float.MAX_VALUE)).toBigInteger()))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getBigIntegerValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Double.MAX_VALUE).toBigInteger()))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getBigIntegerValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getBigIntegerValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.toBigInteger()))));
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigIntegerValue_STRING_ABC_KEY() {
    record.getBigIntegerValue(STRING_ABC_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigIntegerValue_STRING_EMPTY_KEY() {
    record.getBigIntegerValue(STRING_EMPTY_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigIntegerValue_STRING_TRUE_KEY() {
    record.getBigIntegerValue(STRING_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigIntegerValue_STRING_TRUE_UPPERCASE_KEY() {
    record.getBigIntegerValue(STRING_TRUE_UPPERCASE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigIntegerValue_BOOLEAN_TRUE_KEY() {
    record.getBigIntegerValue(BOOLEAN_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigIntegerValue_BOOLEAN_FALSE_KEY() {
    record.getBigIntegerValue(BOOLEAN_FALSE_KEY);
  }

  @Test
  public void testGetBigDecimalValue_MISSING_VALUE_KEY() {
    assertThat(record.getBigDecimalValue(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getBigDecimalValue(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Byte.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getBigDecimalValue(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Short.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getBigDecimalValue(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Integer.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getBigDecimalValue(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Long.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getBigDecimalValue(NUMBER_MAX_FLOAT_KEY),
        // We cannot use BigDecimal.valueOf here, since it only accepts doubles, causing our
        // passed-in float to be promoted to double. This changes precision and no longer passes
        // the test.
        is(optionalWithValue(equalTo(new BigDecimal(String.valueOf(Float.MAX_VALUE))))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getBigDecimalValue(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Double.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getBigDecimalValue(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(new BigDecimal(BIG_INTEGER_TEST_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getBigDecimalValue(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE))));
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigDecimalValue_STRING_ABC_KEY() {
    record.getBigDecimalValue(STRING_ABC_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigDecimalValue_STRING_EMPTY_KEY() {
    record.getBigDecimalValue(STRING_EMPTY_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigDecimalValue_STRING_TRUE_KEY() {
    record.getBigDecimalValue(STRING_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigDecimalValue_STRING_TRUE_UPPERCASE_KEY() {
    record.getBigDecimalValue(STRING_TRUE_UPPERCASE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigDecimalValue_BOOLEAN_TRUE_KEY() {
    record.getBigDecimalValue(BOOLEAN_TRUE_KEY);
  }

  @Test(expected = NumberFormatException.class)
  public void testGetBigDecimalValue_BOOLEAN_FALSE_KEY() {
    record.getBigDecimalValue(BOOLEAN_FALSE_KEY);
  }

  private static final String MISSING_VALUE_KEY = "DOES_NOT_EXIST";
  private static final String NUMBER_MAX_BYTE_KEY = "NumberMaxByte__c";
  private static final String NUMBER_MAX_SHORT_KEY = "NumberMaxShort__c";
  private static final String NUMBER_MAX_INT_KEY = "NumberMaxInt__c";
  private static final String NUMBER_MAX_LONG_KEY = "NumberMaxLong__c";
  private static final String NUMBER_MAX_FLOAT_KEY = "NumberMaxFloat__c";
  private static final String NUMBER_MAX_DOUBLE_KEY = "NumberMaxDouble__c";
  private static final String NUMBER_BIG_INTEGER_KEY = "NumberBigInteger__c";
  private static final String NUMBER_BIG_DECIMAL_KEY = "NumberBigDecimal__c";
  private static final String STRING_ABC_KEY = "String__c";
  private static final String STRING_EMPTY_KEY = "StringEmpty__c";
  private static final String STRING_TRUE_KEY = "StringTrue__c";
  private static final String STRING_TRUE_UPPERCASE_KEY = "StringTrueUppercase__c";
  private static final String BOOLEAN_TRUE_KEY = "BooleanTrue__c";
  private static final String BOOLEAN_FALSE_KEY = "BooleanFalse__c";

  private static final BigInteger BIG_INTEGER_TEST_VALUE =
      BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(2));
  private static final BigDecimal BIG_DECIMAL_TEST_VALUE =
      BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.valueOf(2));
}
