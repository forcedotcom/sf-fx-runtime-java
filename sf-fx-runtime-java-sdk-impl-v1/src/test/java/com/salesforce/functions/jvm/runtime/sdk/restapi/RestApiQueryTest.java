/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RecordBuilder.map;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RecordBuilder.Tuple;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class RestApiQueryTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "51.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void queryWithNextRecordsUrl()
      throws IOException, RestApiErrorsException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT RANDOM_1__c, RANDOM_2__c FROM Random__c");
    QueryRecordResult result = restApi.execute(queryRequest);

    assertThat(result.isDone(), is(false));
    assertThat(result.getTotalSize(), is(10000L));
    assertThat(result.getRecords(), hasSize(2000));

    assertThat(
        result.getNextRecordsPath(),
        is(optionalWithValue(equalTo("/services/data/v51.0/query/01gB000003OCxSPIA1-2000"))));
  }

  @Test
  public void queryMoreRecords() throws IOException, RestApiErrorsException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT RANDOM_1__c, RANDOM_2__c FROM Random__c");
    QueryRecordResult result = restApi.execute(queryRequest);

    QueryNextRecordsRestApiRequest queryMoreRequest =
        new QueryNextRecordsRestApiRequest(result.getNextRecordsPath().get());
    QueryRecordResult result2 = restApi.execute(queryMoreRequest);

    assertThat(result2.getRecords(), hasSize(2000));
  }

  @Test
  public void queryWithUnknownColumn() throws IOException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Bacon__c FROM Account LIMIT 2");

    try {
      restApi.execute(queryRequest);
    } catch (RestApiErrorsException e) {
      assertThat(e.getApiErrors(), hasSize(1));

      RestApiError apiError = e.getApiErrors().get(0);

      assertThat(
          apiError.getMessage(),
          is(
              equalTo(
                  "\nSELECT Bacon__c FROM Account LIMIT 2\n       ^\nERROR at Row:1:Column:8\nNo such column 'Bacon__c' on entity 'Account'. If you are attempting to use a custom field, be sure to append the '__c' after the custom field name. Please reference your WSDL or the describe call for the appropriate names.")));

      assertThat(apiError.getErrorCode(), is(equalTo("INVALID_FIELD")));
      return;
    }

    Assert.fail("Expected exception!");
  }

  @Test
  public void queryWithMalformedSoql() throws IOException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELEKT Name FROM Account");

    try {
      restApi.execute(queryRequest);
    } catch (RestApiErrorsException e) {
      assertThat(e.getApiErrors(), hasSize(1));

      RestApiError apiError = e.getApiErrors().get(0);

      assertThat(apiError.getMessage(), is(equalTo("unexpected token: SELEKT")));

      assertThat(apiError.getErrorCode(), is(equalTo("MALFORMED_QUERY")));
      return;
    }

    Assert.fail("Expected exception!");
  }

  @Test
  public void queryAccountNames() throws IOException, RestApiErrorsException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Name FROM Account");
    QueryRecordResult result = restApi.execute(queryRequest);

    assertThat(result.getTotalSize(), is(5L));
    assertThat(result.isDone(), is(true));
    assertThat(result.getNextRecordsPath(), equalTo(Optional.empty()));

    List<Record> expectedRecords = new ArrayList<>();
    expectedRecords.add(
        new Record(
            map(
                new Tuple("type", "Account"),
                new Tuple("url", "/services/data/v51.0/sobjects/Account/001B000001LntWlIAJ")),
            map(new Tuple("Name", "An awesome test account"))));

    expectedRecords.add(
        new Record(
            map(
                new Tuple("type", "Account"),
                new Tuple("url", "/services/data/v51.0/sobjects/Account/001B000001LwihtIAB")),
            map(new Tuple("Name", "Global Media"))));

    expectedRecords.add(
        new Record(
            map(
                new Tuple("type", "Account"),
                new Tuple("url", "/services/data/v51.0/sobjects/Account/001B000001LwihuIAB")),
            map(new Tuple("Name", "Acme"))));

    expectedRecords.add(
        new Record(
            map(
                new Tuple("type", "Account"),
                new Tuple("url", "/services/data/v51.0/sobjects/Account/001B000001LwihvIAB")),
            map(new Tuple("Name", "salesforce.com"))));

    expectedRecords.add(
        new Record(
            map(
                new Tuple("type", "Account"),
                new Tuple("url", "/services/data/v51.0/sobjects/Account/001B000001LnobCIAR")),
            map(new Tuple("Name", "Sample Account for Entitlements"))));

    assertThat(result.getRecords(), is(equalTo(expectedRecords)));
  }
}
