/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.requests;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApi;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.BulkQueryResults;
import com.salesforce.functions.jvm.runtime.sdk.restapi.csv.CsvTable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;

public class GetBulkQueryJobResultsApiRequestTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void results() throws RestApiErrorsException, IOException, RestApiException {
    GetBulkQueryJobResultsApiRequest request =
        new GetBulkQueryJobResultsApiRequest("7507Q000009f8jvQAA", null, null);
    BulkQueryResults results = restApi.execute(request);

    List<String> expectedHeaders = Arrays.asList("Name", "Type", "Industry");

    List<List<String>> expectedRows = new ArrayList<>();
    expectedRows.add(Arrays.asList("Beispielaccount für Ansprüche", "", ""));
    expectedRows.add(Arrays.asList("Edge Communications", "Customer - Direct", "Electronics"));
    expectedRows.add(
        Arrays.asList("Burlington Textiles Corp of America", "Customer - Direct", "Apparel"));
    expectedRows.add(
        Arrays.asList("Pyramid Construction Inc.", "Customer - Channel", "Construction"));
    expectedRows.add(Arrays.asList("Dickenson plc", "Customer - Channel", "Consulting"));
    expectedRows.add(
        Arrays.asList("Grand Hotels & Resorts Ltd", "Customer - Direct", "Hospitality"));
    expectedRows.add(Arrays.asList("United Oil & Gas Corp.", "Customer - Direct", "Energy"));
    expectedRows.add(
        Arrays.asList("Express Logistics and Transport", "Customer - Channel", "Transportation"));
    expectedRows.add(Arrays.asList("University of Arizona", "Customer - Direct", "Education"));
    expectedRows.add(Arrays.asList("United Oil & Gas, UK", "Customer - Direct", "Energy"));
    expectedRows.add(Arrays.asList("United Oil & Gas, Singapore", "Customer - Direct", "Energy"));
    expectedRows.add(Arrays.asList("GenePoint", "Customer - Channel", "Biotechnology"));
    expectedRows.add(Arrays.asList("sForce", "", ""));

    assertThat(results.getLocator(), is(emptyOptional()));
    assertThat(results.getTable(), is(equalTo(new CsvTable(expectedHeaders, expectedRows))));
  }

  @Test
  public void resultsLimited() throws RestApiErrorsException, IOException, RestApiException {
    GetBulkQueryJobResultsApiRequest request =
        new GetBulkQueryJobResultsApiRequest("7507Q000009f9tHQAQ", null, 10L);
    BulkQueryResults results = restApi.execute(request);

    List<String> expectedHeaders = Arrays.asList("Name", "Type", "Industry");

    List<List<String>> expectedRows = new ArrayList<>();
    expectedRows.add(Arrays.asList("Beispielaccount für Ansprüche", "", ""));
    expectedRows.add(Arrays.asList("Edge Communications", "Customer - Direct", "Electronics"));
    expectedRows.add(
        Arrays.asList("Burlington Textiles Corp of America", "Customer - Direct", "Apparel"));
    expectedRows.add(
        Arrays.asList("Pyramid Construction Inc.", "Customer - Channel", "Construction"));
    expectedRows.add(Arrays.asList("Dickenson plc", "Customer - Channel", "Consulting"));
    expectedRows.add(
        Arrays.asList("Grand Hotels & Resorts Ltd", "Customer - Direct", "Hospitality"));
    expectedRows.add(Arrays.asList("United Oil & Gas Corp.", "Customer - Direct", "Energy"));
    expectedRows.add(
        Arrays.asList("Express Logistics and Transport", "Customer - Channel", "Transportation"));
    expectedRows.add(Arrays.asList("University of Arizona", "Customer - Direct", "Education"));
    expectedRows.add(Arrays.asList("United Oil & Gas, UK", "Customer - Direct", "Energy"));

    assertThat(results.getLocator(), is(optionalWithValue(equalTo("MTA"))));
    assertThat(results.getTable(), is(equalTo(new CsvTable(expectedHeaders, expectedRows))));
  }

  @Test
  public void resultsLimitedNext() throws RestApiErrorsException, IOException, RestApiException {
    GetBulkQueryJobResultsApiRequest request =
        new GetBulkQueryJobResultsApiRequest("7507Q000009f9tHQAQ", "MTA", null);
    BulkQueryResults results = restApi.execute(request);

    List<String> expectedHeaders = Arrays.asList("Name", "Type", "Industry");

    List<List<String>> expectedRows = new ArrayList<>();
    expectedRows.add(Arrays.asList("United Oil & Gas, Singapore", "Customer - Direct", "Energy"));
    expectedRows.add(Arrays.asList("GenePoint", "Customer - Channel", "Biotechnology"));
    expectedRows.add(Arrays.asList("sForce", "", ""));

    assertThat(results.getLocator(), is(emptyOptional()));
    assertThat(results.getTable(), is(equalTo(new CsvTable(expectedHeaders, expectedRows))));
  }
}
