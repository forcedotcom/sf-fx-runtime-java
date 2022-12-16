/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RecordBuilder.*;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class RestApiQueryTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
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
        is(optionalWithValue(equalTo("/services/data/v53.0/query/01gB000003OCxSPIA1-2000"))));
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
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001B000001LntWlIAJ")),
            fields(field("Name", "An awesome test account"))));

    expectedRecords.add(
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001B000001LwihtIAB")),
            fields(field("Name", "Global Media"))));

    expectedRecords.add(
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001B000001LwihuIAB")),
            fields(field("Name", "Acme"))));

    expectedRecords.add(
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001B000001LwihvIAB")),
            fields(field("Name", "salesforce.com"))));

    expectedRecords.add(
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001B000001LnobCIAR")),
            fields(field("Name", "Sample Account for Entitlements"))));

    assertThat(result.getRecords(), is(equalTo(expectedRecords)));
  }

  @Test
  public void queryWithSubQuery() throws IOException, RestApiException, RestApiErrorsException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest(
            "SELECT Account.Name, (SELECT Contact.FirstName, Contact.LastName FROM Account.Contacts) FROM Account LIMIT 5");

    QueryRecordResult result = restApi.execute(queryRequest);

    assertThat(result.isDone(), is(true));

    List<Record> expectedRecords = new ArrayList<>();
    expectedRecords.add(
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/0017Q00000EZlbyQAD")),
            fields(
                field("Name", "GenePoint"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        1,
                        true,
                        Collections.singletonList(
                            new Record(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/0037Q000007vKjpQAE")),
                                fields(field("FirstName", "Edna"), field("LastName", "Frank")))),
                        null)))));

    expectedRecords.add(
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/0017Q00000EZlbwQAD")),
            fields(
                field("Name", "United Oil & Gas, UK"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        1,
                        true,
                        Collections.singletonList(
                            new Record(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/0037Q000007vKjmQAE")),
                                fields(field("FirstName", "Ashley"), field("LastName", "James")))),
                        null)))));

    expectedRecords.add(
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/0017Q00000EZlbxQAD")),
            fields(
                field("Name", "United Oil & Gas, Singapore"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        2,
                        true,
                        Arrays.asList(
                            new Record(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/0037Q000007vKjnQAE")),
                                fields(field("FirstName", "Tom"), field("LastName", "Ripley"))),
                            new Record(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/0037Q000007vKjoQAE")),
                                fields(field("FirstName", "Liz"), field("LastName", "D'Cruz")))),
                        null)))));

    expectedRecords.add(
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/0017Q00000EZlboQAD")),
            fields(
                field("Name", "Edge Communications"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        2,
                        true,
                        Arrays.asList(
                            new Record(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/0037Q000007vKjZQAU")),
                                fields(field("FirstName", "Rose"), field("LastName", "Gonzalez"))),
                            new Record(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/0037Q000007vKjaQAE")),
                                fields(field("FirstName", "Sean"), field("LastName", "Forbes")))),
                        null)))));

    expectedRecords.add(
        new Record(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/0017Q00000EZlbpQAD")),
            fields(
                field("Name", "Burlington Textiles Corp of America"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        1,
                        true,
                        Collections.singletonList(
                            new Record(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/0037Q000007vKjbQAE")),
                                fields(field("FirstName", "Jack"), field("LastName", "Rogers")))),
                        null)))));

    assertThat(result.getRecords(), is(equalTo(expectedRecords)));
  }
}
