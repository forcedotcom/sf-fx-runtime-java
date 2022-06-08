/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiError;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.sdk.bulk.Operation;
import java.io.IOException;
import org.junit.Test;

public class BulkApiRequestCreateJobTest extends BulkApiTestCase {
  private JobInfo createJob(String objectType, Operation operation)
      throws RestApiErrorsException, IOException, RestApiException {
    return api.execute(new BulkApiRequestCreateJob(objectType, operation));
  }

  @Test
  public void create_job_should_process_a_2xx_response()
      throws RestApiErrorsException, IOException, RestApiException {
    stubCreateBulkApiRequest("Account", Operation.INSERT);
    assertThat(createJob("Account", Operation.INSERT).getId(), equalTo(TEST_JOB_ID));
  }

  @Test
  public void create_job_should_process_a_4xx_response() {
    RestApiError restApiError =
        new RestApiError("InvalidJob : Unable to find object: blah", "INVALIDJOB");
    stubCreateBulkApiRequest(
        "badObjectType",
        Operation.UPDATE,
        (jobInfo, responseBuilder) ->
            responseBuilder.withStatus(400).withBody(withErrorResponse(restApiError)));
    RestApiErrorsException e =
        assertThrows(
            RestApiErrorsException.class, () -> createJob("badObjectType", Operation.UPDATE));
    assertThat(e.getApiErrors().size(), equalTo(1));
    assertThat(
        e.getApiErrors().get(0).getMessage(), equalTo("InvalidJob : Unable to find object: blah"));
    assertThat(e.getApiErrors().get(0).getErrorCode(), equalTo("INVALIDJOB"));
  }

  @Test
  public void create_job_should_process_a_5xx_response() {
    stubCreateBulkApiRequest(
        "Account", Operation.UPDATE, (jobInfo, responseBuilder) -> responseBuilder.withStatus(500));
    RestApiException e =
        assertThrows(RestApiException.class, () -> createJob("Account", Operation.UPDATE));
    assertThat(e.getMessage(), equalTo("Could not parse API response as JSON!"));
  }
}
