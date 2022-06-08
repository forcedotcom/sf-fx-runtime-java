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

import com.salesforce.functions.jvm.runtime.sdk.RecordBuilderImpl;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiError;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.sdk.Record;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.entity.ContentType;
import org.junit.Test;

public class BulkApiRequestUploadJobDataTest extends BulkApiTestCase {
  private void uploadJobData(List<Record> records)
      throws RestApiErrorsException, IOException, RestApiException {
    api.execute(new BulkApiRequestUploadJobData(TEST_JOB_ID, records));
  }

  @Test
  public void upload_job_data_should_process_a_2xx_response_when_no_records_are_uploaded()
      throws RestApiErrorsException, IOException, RestApiException {
    stubUploadBulkApiRequest();
    uploadJobData(new ArrayList<>());
  }

  @Test
  public void upload_job_data_should_process_a_2xx_response_when_some_records_are_uploaded()
      throws RestApiErrorsException, IOException, RestApiException {
    List<Record> records = new ArrayList<>();
    records.add(new RecordBuilderImpl("Account").withField("Name", "Account 1").build());
    records.add(new RecordBuilderImpl("Account").withField("Name", "Account 2").build());
    stubUploadBulkApiRequest();
    uploadJobData(records);
  }

  @Test
  public void upload_job_data_should_process_a_4xx_response() {
    RestApiError restApiError =
        new RestApiError("The requested resource does not exist", "NOT_FOUND");
    stubUploadBulkApiRequest(
        (responseBuilder) ->
            responseBuilder
                .withStatus(404)
                .withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
                .withBody(withErrorResponse(restApiError)));
    RestApiErrorsException e =
        assertThrows(RestApiErrorsException.class, () -> uploadJobData(new ArrayList<>()));
    assertThat(e.getApiErrors().size(), equalTo(1));
    assertThat(
        e.getApiErrors().get(0).getMessage(), equalTo("The requested resource does not exist"));
    assertThat(e.getApiErrors().get(0).getErrorCode(), equalTo("NOT_FOUND"));
  }

  @Test
  public void upload_job_data_should_process_a_5xx_response() {
    stubUploadBulkApiRequest((responseBuilder) -> responseBuilder.withStatus(500));
    RestApiException e =
        assertThrows(RestApiException.class, () -> uploadJobData(new ArrayList<>()));
    assertThat(e.getMessage(), equalTo("Could not parse API response as JSON!"));
  }
}
