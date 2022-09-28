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
import static org.hamcrest.Matchers.hasEntry;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class RestApiCompositeTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void compositeSingleCreate() throws RestApiErrorsException, IOException, RestApiException {
    Map<String, RestApiRequest<ModifyRecordResult>> subrequests = new HashMap<>();

    Map<String, JsonElement> values = new HashMap<>();
    values.put("Name", new JsonPrimitive("Star Wars Episode IV - A New Hope"));
    values.put("Rating__c", new JsonPrimitive("Excellent"));
    subrequests.put("insert-anh", new CreateRecordRestApiRequest("Movie__c", values));

    CompositeGraphRestApiRequest<ModifyRecordResult> request =
        new CompositeGraphRestApiRequest<>(
            restApi.getOrgDomainUrl(), restApi.getApiVersion(), subrequests);
    Map<String, ModifyRecordResult> result = restApi.execute(request);

    assertThat(
        result,
        hasEntry(equalTo("insert-anh"), equalTo(new ModifyRecordResult("a01B0000009gSoxIAE"))));
  }

  @Test
  public void compositeSingleCreateWithError() throws IOException, RestApiException {
    Map<String, RestApiRequest<ModifyRecordResult>> subrequests = new HashMap<>();

    Map<String, JsonElement> values = new HashMap<>();
    values.put("Name", new JsonPrimitive("Star Wars Episode IV - A New Hope"));
    values.put("Rating__c", new JsonPrimitive("Amazing"));
    subrequests.put("insert-anh", new CreateRecordRestApiRequest("Movie__c", values));

    CompositeGraphRestApiRequest<ModifyRecordResult> request =
        new CompositeGraphRestApiRequest<>(
            restApi.getOrgDomainUrl(), restApi.getApiVersion(), subrequests);

    try {
      restApi.execute(request);
    } catch (RestApiErrorsException e) {
      assertThat(
          e.getApiErrors(),
          contains(
              equalTo(
                  new RestApiError(
                      "Rating: bad value for restricted picklist field: Amazing",
                      "INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST",
                      Collections.singletonList("Rating__c")))));
      return;
    }

    Assert.fail("Expected exception!");
  }

  @Test
  public void compositeSingleUpdate() throws RestApiErrorsException, IOException, RestApiException {
    Map<String, RestApiRequest<ModifyRecordResult>> subrequests = new HashMap<>();

    Map<String, JsonElement> values = new HashMap<>();
    values.put("ReleaseDate__c", new JsonPrimitive("1980-05-21"));
    subrequests.put(
        "referenceId0", new UpdateRecordRestApiRequest("a01B0000009gSrFIAU", "Movie__c", values));

    CompositeGraphRestApiRequest<ModifyRecordResult> request =
        new CompositeGraphRestApiRequest<>(
            restApi.getOrgDomainUrl(), restApi.getApiVersion(), subrequests);
    Map<String, ModifyRecordResult> result = restApi.execute(request);

    assertThat(
        result,
        hasEntry(equalTo("referenceId0"), equalTo(new ModifyRecordResult("a01B0000009gSrFIAU"))));
  }

  @Test
  public void compositeCreateTree() throws RestApiErrorsException, IOException, RestApiException {
    Map<String, RestApiRequest<ModifyRecordResult>> subrequests = new LinkedHashMap<>();

    Map<String, JsonElement> valuesFranchise = new HashMap<>();
    valuesFranchise.put("Name", new JsonPrimitive("Star Wars"));
    subrequests.put(
        "referenceId0", new CreateRecordRestApiRequest("Franchise__c", valuesFranchise));

    Map<String, JsonElement> valuesEp1 = new HashMap<>();
    valuesEp1.put("Name", new JsonPrimitive("Star Wars Episode I - A Phantom Menace"));
    valuesEp1.put("Franchise__c", new JsonPrimitive("@{referenceId0.id}"));
    subrequests.put("referenceId1", new CreateRecordRestApiRequest("Movie__c", valuesEp1));

    Map<String, JsonElement> valuesEp2 = new HashMap<>();
    valuesEp2.put("Name", new JsonPrimitive("Star Wars Episode II - Attack Of The Clones"));
    valuesEp2.put("Franchise__c", new JsonPrimitive("@{referenceId0.id}"));
    subrequests.put("referenceId2", new CreateRecordRestApiRequest("Movie__c", valuesEp2));

    Map<String, JsonElement> valuesEp3 = new HashMap<>();
    valuesEp3.put("Name", new JsonPrimitive("Star Wars Episode III - Revenge Of The Sith"));
    valuesEp3.put("Franchise__c", new JsonPrimitive("@{referenceId0.id}"));
    subrequests.put("referenceId3", new CreateRecordRestApiRequest("Movie__c", valuesEp3));

    CompositeGraphRestApiRequest<ModifyRecordResult> request =
        new CompositeGraphRestApiRequest<>(
            restApi.getOrgDomainUrl(), restApi.getApiVersion(), subrequests);
    Map<String, ModifyRecordResult> result = restApi.execute(request);

    assertThat(
        result,
        hasEntry(equalTo("referenceId0"), equalTo(new ModifyRecordResult("a03B0000007BhQQIA0"))));

    assertThat(
        result,
        hasEntry(equalTo("referenceId1"), equalTo(new ModifyRecordResult("a00B000000FSkioIAD"))));

    assertThat(
        result,
        hasEntry(equalTo("referenceId2"), equalTo(new ModifyRecordResult("a00B000000FSkipIAD"))));

    assertThat(
        result,
        hasEntry(equalTo("referenceId3"), equalTo(new ModifyRecordResult("a00B000000FSkiqIAD"))));
  }

  @Test
  public void compositeDeleteTest() throws RestApiErrorsException, IOException, RestApiException {
    Map<String, RestApiRequest<ModifyRecordResult>> subrequests = new LinkedHashMap<>();
    subrequests.put(
        "referenceId0", new DeleteRecordRestApiRequest("Movie__c", "a01B0000009gSr9IAE"));

    CompositeGraphRestApiRequest<ModifyRecordResult> request =
        new CompositeGraphRestApiRequest<>(
            restApi.getOrgDomainUrl(), restApi.getApiVersion(), subrequests);
    Map<String, ModifyRecordResult> result = restApi.execute(request);

    assertThat(
        result,
        hasEntry(equalTo("referenceId0"), equalTo(new ModifyRecordResult("a01B0000009gSr9IAE"))));
  }
}
