/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiError;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.junit.Test;

public class BulkApiJobRequestCloseJobTest extends BulkApiTestCase {
  private void closeJob() throws RestApiErrorsException, IOException, RestApiException {
    api.execute(new BulkApiJobRequestCloseJob(TEST_JOB_ID));
  }

  @Test
  public void close_job_should_process_a_2xx_response()
      throws RestApiErrorsException, IOException, RestApiException {
    stubCloseBulkApiRequest();
    closeJob();
  }

  @Test
  public void close_job_should_process_a_4xx_response() {
    RestApiError restApiError =
        new RestApiError(
            "Could not find content for job: <750B000000EJgWV>, try doing 'PUT' on 'contentUrl' corresponding to this job and once upload finished use 'PATCH' to mark Job as 'UploadComplete'",
            "INVALIDJOB");
    stubCloseBulkApiRequest(
        (responseBuilder) ->
            responseBuilder.withStatus(404).withBody(withErrorResponse(restApiError)));
    RestApiErrorsException e = assertThrows(RestApiErrorsException.class, this::closeJob);
    assertThat(e.getApiErrors().size(), Matchers.equalTo(1));
    assertThat(
        e.getApiErrors().get(0).getMessage(),
        Matchers.equalTo(
            "Could not find content for job: <750B000000EJgWV>, try doing 'PUT' on 'contentUrl' corresponding to this job and once upload finished use 'PATCH' to mark Job as 'UploadComplete'"));
    assertThat(e.getApiErrors().get(0).getErrorCode(), Matchers.equalTo("INVALIDJOB"));
  }

  @Test
  public void close_job_should_process_a_5xx_response() {
    stubCloseBulkApiRequest((responseBuilder) -> responseBuilder.withStatus(500));
    RestApiException e = assertThrows(RestApiException.class, this::closeJob);
    assertThat(e.getMessage(), Matchers.equalTo("Could not parse API response as JSON!"));
  }
}
