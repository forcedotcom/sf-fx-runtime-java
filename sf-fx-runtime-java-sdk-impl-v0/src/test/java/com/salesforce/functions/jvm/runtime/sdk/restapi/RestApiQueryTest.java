/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public void queryWithNextRecordsUrl() throws IOException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT RANDOM_1__c, RANDOM_2__c FROM Random__c");
    QueryRecordResult result = restApi.execute(queryRequest);

    assertThat("the result is not done", result.isDone(), is(false));
    assertThat("the total size is 10000", result.getTotalSize(), is(10000L));
    assertThat(
        "the next records path is present",
        result.getNextRecordsPath(),
        equalTo(Optional.of("/services/data/v51.0/query/01gB000003OCxSPIA1-2000")));
  }

  @Test
  public void queryMoreRecords() throws IOException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT RANDOM_1__c, RANDOM_2__c FROM Random__c");
    QueryRecordResult result = restApi.execute(queryRequest);

    QueryNextRecordsRestApiRequest queryMoreRequest =
        new QueryNextRecordsRestApiRequest(result.getNextRecordsPath().get());
    QueryRecordResult result2 = restApi.execute(queryMoreRequest);

    assertThat("2000 records are present", result2.getRecords().size(), equalTo(2000));
  }

  @Test
  public void queryWithUnknownColumn() throws IOException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Bacon__c FROM Account LIMIT 2");

    try {
      restApi.execute(queryRequest);
    } catch (RestApiException e) {
      assertThat("Exactly one error is returned", e.getApiErrors().size(), is(1));

      RestApiError apiError = e.getApiErrors().get(0);

      assertThat(
          "The error has the correct message",
          apiError.getMessage(),
          equalTo(
              "\nSELECT Bacon__c FROM Account LIMIT 2\n       ^\nERROR at Row:1:Column:8\nNo such column 'Bacon__c' on entity 'Account'. If you are attempting to use a custom field, be sure to append the '__c' after the custom field name. Please reference your WSDL or the describe call for the appropriate names."));

      assertThat(
          "The error has the correct code", apiError.getErrorCode(), equalTo("INVALID_FIELD"));
      return;
    }

    Assert.fail("Expected exception!");
  }

  @Test
  public void queryWithMalformedSoql() throws IOException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELEKT Name FROM Account");

    try {
      restApi.execute(queryRequest);
    } catch (RestApiException e) {
      assertThat("Exactly one error is returned", e.getApiErrors().size(), is(1));

      RestApiError apiError = e.getApiErrors().get(0);

      assertThat(
          "The error has the correct message",
          apiError.getMessage(),
          equalTo("unexpected token: SELEKT"));

      assertThat(
          "The error has the correct code", apiError.getErrorCode(), equalTo("MALFORMED_QUERY"));
      return;
    }

    Assert.fail("Expected exception!");
  }

  @Test
  public void queryAccountNames() throws IOException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Name FROM Account");
    QueryRecordResult result = restApi.execute(queryRequest);

    assertThat("total size is correct", result.getTotalSize(), is(5L));
    assertThat("is done is true", result.isDone(), is(true));
    assertThat("next record path is empty", result.getNextRecordsPath(), equalTo(Optional.empty()));

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

    assertThat("records match", result.getRecords(), equalTo(expectedRecords));
  }

  private static Map<String, JsonPrimitive> map(Tuple... data) {
    HashMap<String, JsonPrimitive> result = new HashMap<>();
    for (Tuple tuple : data) {
      result.put(tuple.getKey(), tuple.getValue());
    }

    return result;
  }

  private static class Tuple {
    private final String key;
    private final JsonPrimitive value;

    public Tuple(String key, String value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public Tuple(String key, Number value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public Tuple(String key, Boolean value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public Tuple(String key, Character value) {
      this.key = key;
      this.value = new JsonPrimitive(value);
    }

    public String getKey() {
      return key;
    }

    public JsonPrimitive getValue() {
      return value;
    }
  }
}
