/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RecordBuilder.*;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class QueryRecordResultTest {

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

    EqualsVerifier.forClass(QueryRecordResult.class)
        .withPrefabValues(Record.class, red, blue)
        .verify();
  }
}
