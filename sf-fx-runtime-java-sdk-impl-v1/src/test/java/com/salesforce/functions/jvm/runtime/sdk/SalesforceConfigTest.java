/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

public class SalesforceConfigTest {

  private SalesforceConfig salesforceConfig;
  private static final String PROJECT_TOML = "project.toml";

  @Before
  public void setUp() {
    salesforceConfig = new SalesforceConfig();
  }

  @Test
  public void testGetId() {
    assertThat(salesforceConfig.getId(), is(equalTo("test-project-toml")));
  }

  @Test
  public void testGetSchemaVersion() {
    assertThat(salesforceConfig.getSchemaVersion(), is(equalTo("0.1")));
  }

  @Test
  public void testGetDescription() {
    assertThat(salesforceConfig.getDescription(), is(equalTo("A Salesforce Function")));
  }

  @Test
  public void testGetType() {
    assertThat(salesforceConfig.getType(), is(equalTo("function")));
  }

  @Test
  public void testGetSalesforceApiVersionWithProjectToml() {
    assertThat(salesforceConfig.getSalesforceApiVersion(), is(equalTo("51.0")));
  }
}
