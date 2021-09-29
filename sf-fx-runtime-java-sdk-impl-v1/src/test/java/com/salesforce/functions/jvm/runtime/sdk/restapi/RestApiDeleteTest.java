/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.net.URI;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class RestApiDeleteTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void delete() throws RestApiErrorsException, IOException, RestApiException {
    DeleteRecordRestApiRequest deleteApiRequest =
        new DeleteRecordRestApiRequest("Account", "001B000001Lp1FxIAJ");

    ModifyRecordResult result = restApi.execute(deleteApiRequest);

    assertThat(result.getId(), is(equalTo("001B000001Lp1FxIAJ")));
  }

  @Test
  public void deleteAlreadyDeletedTest() throws IOException, RestApiException {
    DeleteRecordRestApiRequest deleteApiRequest =
        new DeleteRecordRestApiRequest("Account", "001B000001Lp1G2IAJ");

    try {
      restApi.execute(deleteApiRequest);
    } catch (RestApiErrorsException e) {
      assertThat(
          e.getApiErrors(),
          contains(
              pojo(RestApiError.class)
                  .withProperty("message", is(equalTo("entity is deleted")))
                  .withProperty("errorCode", is(equalTo("ENTITY_IS_DELETED")))
                  .withProperty("fields", is(empty()))));

      return;
    }

    Assert.fail("Expected RestApiErrorsException!");
  }
}
