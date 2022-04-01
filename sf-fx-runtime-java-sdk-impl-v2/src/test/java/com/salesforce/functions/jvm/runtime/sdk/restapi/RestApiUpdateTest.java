/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class RestApiUpdateTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void update() throws RestApiErrorsException, IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("ReleaseDate__c", new JsonPrimitive("1980-05-21"));

    UpdateRecordRestApiRequest request =
        new UpdateRecordRestApiRequest("a00B000000FSjVUIA1", "Movie__c", values);

    ModifyRecordResult result = restApi.execute(request);
    assertThat(result.getId(), is(equalTo("a00B000000FSjVUIA1")));
  }

  @Test
  public void updateWithMalformedId() throws IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("ReleaseDate__c", new JsonPrimitive("1980-05-21"));

    UpdateRecordRestApiRequest request =
        new UpdateRecordRestApiRequest("a00B000000FSjVUIB1", "Movie__c", values);

    try {
      restApi.execute(request);
    } catch (RestApiErrorsException e) {
      RestApiError error = e.getApiErrors().get(0);

      assertThat(
          error.getMessage(), equalTo("Record ID: id value of incorrect type: a00B000000FSjVUIB1"));

      assertThat(error.getErrorCode(), is(equalTo("MALFORMED_ID")));
      assertThat(error.getFields(), contains("Id"));

      return;
    }

    Assert.fail("Expected exception!");
  }

  @Test
  public void updateWithInvalidField() throws IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("Color__c", new JsonPrimitive("Red"));

    UpdateRecordRestApiRequest request =
        new UpdateRecordRestApiRequest("a00B000000FSjVUIB1", "Movie__c", values);

    try {
      restApi.execute(request);
    } catch (RestApiErrorsException e) {
      RestApiError error = e.getApiErrors().get(0);

      assertThat(
          error.getMessage(), equalTo("No such column 'Color__c' on sobject of type Movie__c"));

      assertThat(error.getErrorCode(), is(equalTo("INVALID_FIELD")));
      assertThat(error.getFields(), is(empty()));

      return;
    }

    Assert.fail("Expected exception!");
  }
}
