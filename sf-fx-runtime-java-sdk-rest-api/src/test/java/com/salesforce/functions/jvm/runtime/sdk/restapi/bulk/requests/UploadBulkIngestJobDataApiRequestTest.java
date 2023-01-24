/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.requests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApi;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.ModifyBulkIngestJobResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.csv.CsvTable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;

public class UploadBulkIngestJobDataApiRequestTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void upload() throws RestApiErrorsException, IOException, RestApiException {
    List<String> headers = new ArrayList<>();
    headers.add("Name");
    headers.add("Employees");

    List<List<String>> rows = new ArrayList<>();
    List<String> row1 = new ArrayList<>();
    row1.add("Foo");
    row1.add("12");
    rows.add(row1);

    List<String> row2 = new ArrayList<>();
    row2.add("Bar");
    row2.add("1138");
    rows.add(row2);

    UploadBulkIngestJobDataApiRequest request =
        new UploadBulkIngestJobDataApiRequest("7507Q000009f52jQAA", new CsvTable(headers, rows));
    ModifyBulkIngestJobResult result = restApi.execute(request);

    assertThat(result.getJobId(), is(equalTo("7507Q000009f52jQAA")));
  }
}
