/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonPrimitive;
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

    Record record = new Record(attributes, values);
    assertThat(record.getAttributes(), is(equalTo(attributes)));
    assertThat(record.getValues(), is(equalTo(values)));
  }

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(Record.class).verify();
  }
}
