/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RecordBuilder.jsonPrimitiveMap;

import java.util.Collections;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class QueryRecordResultTest {

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

    EqualsVerifier.forClass(QueryRecordResult.class)
        .withPrefabValues(Record.class, red, blue)
        .verify();
  }
}
