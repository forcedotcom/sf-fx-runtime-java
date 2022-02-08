/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class ProjectMetadataParserTest {
  @Test
  public void testCanonicalExample() {
    assertThat(
        ProjectMetadataParser.parse(
            "[_]\n"
                + "schema-version = \"0.2\"\n"
                + "[com.salesforce]\n"
                + "schema-version = \"0.2\"\n"
                + "id = \"template\"\n"
                + "description = \"This is a Salesforce Function.\"\n"
                + "type = \"function\"\n"
                + "salesforce-api-version = \"55.0\"\n"),
        is(
            optionalWithValue(
                hasProperty("salesforceApiVersion", optionalWithValue(equalTo("55.0"))))));
  }

  @Test
  public void testModifiedExample() {
    assertThat(
        ProjectMetadataParser.parse("com.salesforce.salesforce-api-version = \"60.0\"\n"),
        is(
            optionalWithValue(
                hasProperty("salesforceApiVersion", optionalWithValue(equalTo("60.0"))))));
  }

  @Test
  public void testInvalidToml() {
    assertThat(ProjectMetadataParser.parse("a ="), is(emptyOptional()));
  }

  @Test
  public void testMissingSalesforceApiVersion() {
    assertThat(
        ProjectMetadataParser.parse(
            "[_]\n"
                + "schema-version = \"0.2\"\n"
                + "[com.salesforce]\n"
                + "schema-version = \"0.2\"\n"
                + "id = \"template\"\n"
                + "description = \"This is a Salesforce Function.\"\n"
                + "type = \"function\"\n"),
        is(optionalWithValue(hasProperty("salesforceApiVersion", emptyOptional()))));
  }
}
