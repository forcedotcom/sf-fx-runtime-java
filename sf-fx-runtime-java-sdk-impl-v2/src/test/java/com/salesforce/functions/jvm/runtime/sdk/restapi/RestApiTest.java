/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.net.URI;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class RestApiTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void testUnexpectedResponse() throws IOException, RestApiErrorsException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Name FROM FruitVendor__c");

    try {
      restApi.execute(queryRequest);
    } catch (RestApiException e) {
      assertThat(e.getMessage(), startsWith("Could not parse API response as JSON!"));

      return;
    }

    Assert.fail("Expected Exception!");
  }

  @Test
  public void testApiVersionGetter() {
    assertThat(restApi.getApiVersion(), is(equalTo("53.0")));
  }

  @Test
  public void testAccessTokenGetter() {
    assertThat(
        restApi.getAccessToken(),
        is(
            equalTo(
                "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS")));
  }
}
