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
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.*;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import org.junit.Rule;
import org.junit.Test;

public class GetBulkQueryJobInfoApiRequestTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final RestApi restApi =
      new RestApi(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void getInfo() throws RestApiErrorsException, IOException, RestApiException {
    GetBulkQueryJobInfoApiRequest request = new GetBulkQueryJobInfoApiRequest("7507Q000009f8jvQAA");
    BulkQueryJobInfo result = restApi.execute(request);

    assertThat(
        result,
        is(
            equalTo(
                new BulkQueryJobInfo(
                    "7507Q000009f8jvQAA",
                    BulkQueryOperation.QUERY,
                    "Account",
                    "0057Q0000037T8QQAU",
                    Instant.parse("2023-01-26T14:50:10Z"),
                    Instant.parse("2023-01-26T14:50:12Z"),
                    BulkJobState.JOB_COMPLETE,
                    ContentType.CSV,
                    "53.0",
                    LineEnding.LF,
                    ColumnDelimiter.COMMA,
                    13,
                    0,
                    Duration.ofMillis(176)))));
  }
}
