/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ReferenceIdImplTest {

  @Test
  public void testToApiString() {
    ReferenceIdImpl referenceId = new ReferenceIdImpl("foo");
    assertThat(referenceId.toApiString(), is(equalTo("@{foo.id}")));
  }

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(ReferenceIdImpl.class).verify();
  }
}
