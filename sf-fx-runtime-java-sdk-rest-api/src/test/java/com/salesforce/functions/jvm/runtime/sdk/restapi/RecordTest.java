/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RecordBuilder.jsonPrimitiveMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonPrimitive;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class RecordTest {

  @Test
  public void testValuesAndAttributesAreUnmodified() {
    Map<String, JsonPrimitive> attributes = new HashMap<>();

    Map<String, JsonPrimitive> values = new HashMap<>();
    values.put("foo", new JsonPrimitive("bar"));
    values.put("bar", new JsonPrimitive("baz"));

    Map<String, QueryRecordResult> subQueryResults = new HashMap<>();

    Record record = new Record(attributes, values, subQueryResults);
    assertThat(record.getAttributes(), is(equalTo(attributes)));
    assertThat(record.getValues(), is(equalTo(values)));
    assertThat(record.getSubQueryResults(), is(equalTo(subQueryResults)));
  }

  @Test
  public void testEqualsAndHashCode() {
    Record red =
        new Record(
            jsonPrimitiveMap(
                new RecordBuilder.JsonPrimitiveTuple("type", "Account"),
                new RecordBuilder.JsonPrimitiveTuple(
                    "url", "/services/data/v53.0/sobjects/Account/001B000001LwihuIAB")),
            jsonPrimitiveMap(new RecordBuilder.JsonPrimitiveTuple("Name", "Acme")),
            Collections.emptyMap());

    Record blue =
        new Record(
            jsonPrimitiveMap(
                new RecordBuilder.JsonPrimitiveTuple("type", "Account"),
                new RecordBuilder.JsonPrimitiveTuple(
                    "url", "/services/data/v53.0/sobjects/Account/001B000001LnobCIAR")),
            jsonPrimitiveMap(
                new RecordBuilder.JsonPrimitiveTuple("Name", "Sample Account for Entitlements")),
            Collections.emptyMap());

    EqualsVerifier.forClass(Record.class).withPrefabValues(Record.class, red, blue).verify();
  }
}
