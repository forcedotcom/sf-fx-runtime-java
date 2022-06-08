/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import static com.salesforce.functions.jvm.runtime.sdk.bulk.JobInfoBuilder.createJobInfoBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class JobInfoTest {
  @Test
  public void testEquality() {
    EqualsVerifier.forClass(JobInfo.class).verify();
  }

  @Test
  public void testToString() {
    assertThat(
        createJobInfoBuilder().build().toString(),
        equalTo("JobInfo{id='null', objectType='null', operation=null, jobState=null}"));
  }
}
