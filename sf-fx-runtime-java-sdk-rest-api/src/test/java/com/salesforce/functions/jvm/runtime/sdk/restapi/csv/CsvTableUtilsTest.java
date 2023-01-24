/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.csv;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class CsvTableUtilsTest {
  @Test
  public void baseSerialize() {
    List<String> headers = new ArrayList<>();
    headers.add("Foo");
    headers.add("Bar");
    headers.add("Baz");

    List<String> row1 = new ArrayList<>();
    row1.add("f1");
    row1.add("b1");
    row1.add("b1");

    List<String> row2 = new ArrayList<>();
    row2.add("f2");
    row2.add("b2");
    row2.add("b2");

    List<String> row3 = new ArrayList<>();
    row3.add("f3");
    row3.add("b3");
    row3.add("b3");

    List<List<String>> rows = new ArrayList<>();
    rows.add(row1);
    rows.add(row2);
    rows.add(row3);

    CsvTable csvTable = new CsvTable(headers, rows);

    assertThat(
        new String(CsvTableUtils.serialize(csvTable), StandardCharsets.UTF_8),
        is(equalTo("Foo,Bar,Baz\nf1,b1,b1\nf2,b2,b2\nf3,b3,b3\n")));
  }

  @Test
  public void baseDeserialize() throws CsvException {
    CsvTable table =
        CsvTableUtils.deserialize("Foo,Bar,Baz\r\na,b,c\r\n".getBytes(StandardCharsets.UTF_8));

    List<String> expectedHeaders = new ArrayList<>();
    expectedHeaders.add("Foo");
    expectedHeaders.add("Bar");
    expectedHeaders.add("Baz");

    List<List<String>> expectedRows = new ArrayList<>();
    List<String> expectedRow1 = new ArrayList<>();
    expectedRow1.add("a");
    expectedRow1.add("b");
    expectedRow1.add("c");

    expectedRows.add(expectedRow1);

    assertThat(table.getHeaders(), is(equalTo(expectedHeaders)));
    assertThat(table.getRows(), is(equalTo(expectedRows)));
  }
}
