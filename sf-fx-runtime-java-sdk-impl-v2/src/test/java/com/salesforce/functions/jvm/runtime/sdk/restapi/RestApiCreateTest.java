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

public class RestApiCreateTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void create() throws RestApiErrorsException, IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("Name", new JsonPrimitive("Star Wars Episode V: The Empire Strikes Back"));
    values.put("Rating__c", new JsonPrimitive("Excellent"));

    CreateRecordRestApiRequest request = new CreateRecordRestApiRequest("Movie__c", values);
    ModifyRecordResult result = restApi.execute(request);
    assertThat(result.getId(), is(equalTo("a00B000000FSkcvIAD")));
  }

  @Test
  public void createWithInvalidPicklistValue() throws IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("Name", new JsonPrimitive("Star Wars Episode VIII: The Last Jedi"));
    values.put("Rating__c", new JsonPrimitive("Terrible"));

    CreateRecordRestApiRequest request = new CreateRecordRestApiRequest("Movie__c", values);

    try {
      restApi.execute(request);
    } catch (RestApiErrorsException e) {
      RestApiError error = e.getApiErrors().get(0);

      assertThat(
          error.getMessage(),
          is(equalTo("Rating: bad value for restricted picklist field: Terrible")));

      assertThat(error.getErrorCode(), is(equalTo("INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST")));

      assertThat(error.getFields(), contains("Rating__c"));

      return;
    }

    Assert.fail("Expected exception!");
  }

  @Test
  public void createWithUnknownObjectType() throws IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("Name", new JsonPrimitive("Ace of Spades"));

    CreateRecordRestApiRequest request = new CreateRecordRestApiRequest("PlayingCard__c", values);

    try {
      restApi.execute(request);
    } catch (RestApiErrorsException e) {
      RestApiError error = e.getApiErrors().get(0);

      assertThat(error.getMessage(), equalTo("The requested resource does not exist"));

      assertThat(error.getErrorCode(), is(equalTo("NOT_FOUND")));

      assertThat(error.getFields(), is(empty()));

      return;
    }

    Assert.fail("Expected exception!");
  }

  @Test
  public void createWithInvalidField() throws IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("FavoritePet__c", new JsonPrimitive("Dog"));

    CreateRecordRestApiRequest request = new CreateRecordRestApiRequest("Account", values);

    try {
      restApi.execute(request);
    } catch (RestApiErrorsException e) {
      RestApiError error = e.getApiErrors().get(0);

      assertThat(
          error.getMessage(),
          equalTo("No such column 'FavoritePet__c' on sobject of type Account"));

      assertThat(error.getErrorCode(), is(equalTo("INVALID_FIELD")));

      assertThat(error.getFields(), is(empty()));

      return;
    }

    Assert.fail("Expected exception!");
  }

  @Test
  public void createWithRequiredFieldMissing() throws IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("Name", new JsonPrimitive("Falcon 9"));

    CreateRecordRestApiRequest request = new CreateRecordRestApiRequest("Spaceship__c", values);

    try {
      restApi.execute(request);
    } catch (RestApiErrorsException e) {
      RestApiError error = e.getApiErrors().get(0);

      assertThat(error.getMessage(), equalTo("Required fields are missing: [Website__c]"));

      assertThat(error.getErrorCode(), equalTo("REQUIRED_FIELD_MISSING"));

      assertThat(error.getFields(), contains("Website__c"));

      return;
    }

    Assert.fail("Expected exception!");
  }
}
