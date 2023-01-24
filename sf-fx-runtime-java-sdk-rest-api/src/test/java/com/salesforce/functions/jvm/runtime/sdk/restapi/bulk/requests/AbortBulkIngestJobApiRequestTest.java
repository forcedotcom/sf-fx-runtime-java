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
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiError;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.ModifyBulkIngestJobResult;
import java.io.IOException;
import java.net.URI;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class AbortBulkIngestJobApiRequestTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void abort() throws RestApiErrorsException, IOException, RestApiException {
    AbortBulkIngestJobApiRequest request = new AbortBulkIngestJobApiRequest("7507Q000009f4YjQAI");
    ModifyBulkIngestJobResult result = restApi.execute(request);

    assertThat(result.getJobId(), is(equalTo("7507Q000009f4YjQAI")));
  }

  @Test
  public void abortNonExistent() throws IOException, RestApiException {
    AbortBulkIngestJobApiRequest request = new AbortBulkIngestJobApiRequest("7507Q000009f4YjXXX");

    try {
      restApi.execute(request);
    } catch (RestApiErrorsException e) {
      RestApiError error = e.getApiErrors().get(0);

      assertThat(error.getMessage(), is(equalTo("The requested resource does not exist")));

      assertThat(error.getErrorCode(), is(equalTo("NOT_FOUND")));

      return;
    }

    Assert.fail("Expected exception!");
  }
}
