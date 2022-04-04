/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class QueryRecordResultTest {

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(QueryRecordResult.class).verify();
  }
}
