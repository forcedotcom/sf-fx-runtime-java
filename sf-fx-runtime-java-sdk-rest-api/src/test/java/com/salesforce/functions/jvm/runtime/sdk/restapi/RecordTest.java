/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RecordBuilder.*;
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
    Map<String, JsonPrimitive> values = new HashMap<>();
    values.put("foo", new JsonPrimitive("bar"));
    values.put("bar", new JsonPrimitive("baz"));

    Record record = new Record(attributes(), fields(field("foo", "bar"), field("bar", "baz")));
    assertThat(record.getAttributes().size(), is(equalTo(0)));
    assertThat(record.getValues().get("foo").getJsonData().getAsString(), is(equalTo("bar")));
    assertThat(record.getValues().get("bar").getJsonData().getAsString(), is(equalTo("baz")));
  }

  @Test
  public void testEqualsAndHashCode() {
    Record red =
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001B000001LwihuIAB")),
            fields(field("Name", "Acme")));

    Record blue =
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001B000001LnobCIAR")),
            fields(field("Name", "Sample Account for Entitlements")));

    EqualsVerifier.forClass(Record.class).withPrefabValues(Record.class, red, blue).verify();
  }
}
