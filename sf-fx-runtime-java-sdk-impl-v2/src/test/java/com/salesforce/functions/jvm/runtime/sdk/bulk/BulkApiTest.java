/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.salesforce.functions.jvm.runtime.sdk.bulk.JobInfoBuilder.createJobInfoBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import com.salesforce.functions.jvm.runtime.sdk.RecordBuilderImpl;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiError;
import com.salesforce.functions.jvm.sdk.Record;
import com.salesforce.functions.jvm.sdk.bulk.Job;
import com.salesforce.functions.jvm.sdk.bulk.JobBatchResult;
import com.salesforce.functions.jvm.sdk.bulk.JobState;
import com.salesforce.functions.jvm.sdk.bulk.Operation;
import com.salesforce.functions.jvm.sdk.bulk.error.BulkApiException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;

public class BulkApiTest extends BulkApiTestCase {
  private final long ONE_MB = 1_000_000L;
  private final long SIZE_100_MB = 100 * ONE_MB;

  private BulkApiImpl bulkApi;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    bulkApi = new BulkApiImpl(api);
  }

  @Test
  public void submit_job_should_handle_up_to_100_mb_of_data_in_a_single_job() {
    List<Record> records = createRecordsUpToSizeInBytes(SIZE_100_MB);
    Job job = bulkApi.newJobBuilder("Account", Operation.INSERT, records).build();

    stubCreateBulkApiRequest(job);
    stubUploadBulkApiRequest();
    stubCloseBulkApiRequest();

    List<JobBatchResult> jobBatchResults = bulkApi.submit(job);
    assertThat(jobBatchResults, hasSize(1));
    assertJobBatchResultSuccess(jobBatchResults.get(0));

    verify(1, postRequestedFor(urlEqualTo(bulkApiEndpoint)));
    verify(1, putRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches")));
    verify(1, patchRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID)));
  }

  @Test
  public void submit_job_should_handle_greater_than_100_mb_of_data_in_a_multiple_jobs() {
    List<Record> records = createRecordsUpToSizeInBytes(SIZE_100_MB + ONE_MB);
    Job job = bulkApi.newJobBuilder("Account", Operation.INSERT, records).build();

    stubCreateBulkApiRequest(job);
    stubUploadBulkApiRequest();
    stubCloseBulkApiRequest();

    List<JobBatchResult> jobBatchResults = bulkApi.submit(job);
    assertThat(jobBatchResults, hasSize(2));
    assertJobBatchResultSuccess(jobBatchResults.get(0));
    assertJobBatchResultSuccess(jobBatchResults.get(1));

    verify(2, postRequestedFor(urlEqualTo(bulkApiEndpoint)));
    verify(2, putRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches")));
    verify(2, patchRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID)));
  }

  @Test
  public void submit_job_should_handle_4xx_error_during_job_create() {
    RestApiError restApiError =
        new RestApiError("InvalidJob : Unable to find object: blah", "INVALIDJOB");
    List<Record> records = createRecordsUpToSizeInBytes(ONE_MB);
    Job job = bulkApi.newJobBuilder("Account", Operation.UPDATE, records).build();

    stubCreateBulkApiRequest(
        job,
        ((jobInfo, responseDefinitionBuilder) ->
            responseDefinitionBuilder.withStatus(400).withBody(withErrorResponse(restApiError))));
    stubUploadBulkApiRequest();
    stubCloseBulkApiRequest();

    List<JobBatchResult> jobBatchResults = bulkApi.submit(job);
    assertThat(jobBatchResults, hasSize(1));
    assertJobBatchResultError(
        jobBatchResults.get(0),
        records,
        "One or more API errors occurred:\n\nCode: INVALIDJOB\nMessage: InvalidJob : Unable to find object: blah\n");

    verify(1, postRequestedFor(urlEqualTo(bulkApiEndpoint)));
    verify(0, putRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches")));
    verify(0, patchRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID)));
  }

  @Test
  public void submit_job_should_handle_5xx_error_during_job_create() {
    List<Record> records = createRecordsUpToSizeInBytes(ONE_MB);
    Job job = bulkApi.newJobBuilder("Account", Operation.UPDATE, records).build();

    stubCreateBulkApiRequest(
        job, ((jobInfo, responseDefinitionBuilder) -> responseDefinitionBuilder.withStatus(500)));
    stubUploadBulkApiRequest();
    stubCloseBulkApiRequest();

    List<JobBatchResult> jobBatchResults = bulkApi.submit(job);
    assertThat(jobBatchResults, hasSize(1));
    assertJobBatchResultError(
        jobBatchResults.get(0), records, "Exception while executing API request!");

    verify(1, postRequestedFor(urlEqualTo(bulkApiEndpoint)));
    verify(0, putRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches")));
    verify(0, patchRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID)));
  }

  @Test
  public void submit_job_should_handle_4xx_error_during_job_upload() {
    RestApiError restApiError =
        new RestApiError("The requested resource does not exist", "NOT_FOUND");
    List<Record> records = createRecordsUpToSizeInBytes(ONE_MB);
    Job job = bulkApi.newJobBuilder("Account", Operation.UPSERT, records).build();

    stubCreateBulkApiRequest(job);
    stubUploadBulkApiRequest(
        (responseDefinitionBuilder ->
            responseDefinitionBuilder
                .withStatus(400)
                .withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
                .withBody(withErrorResponse(restApiError))));
    stubCloseBulkApiRequest();

    List<JobBatchResult> jobBatchResults = bulkApi.submit(job);
    assertThat(jobBatchResults, hasSize(1));
    assertJobBatchResultError(
        jobBatchResults.get(0),
        records,
        "One or more API errors occurred:\n\nCode: NOT_FOUND\nMessage: The requested resource does not exist\n");

    verify(1, postRequestedFor(urlEqualTo(bulkApiEndpoint)));
    verify(1, putRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches")));
    verify(0, patchRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID)));
  }

  @Test
  public void submit_job_should_handle_5xx_error_during_job_upload() {
    List<Record> records = createRecordsUpToSizeInBytes(ONE_MB);
    Job job = bulkApi.newJobBuilder("Account", Operation.UPSERT, records).build();

    stubCreateBulkApiRequest(job);
    stubUploadBulkApiRequest(
        (responseDefinitionBuilder -> responseDefinitionBuilder.withStatus(500)));
    stubCloseBulkApiRequest();

    List<JobBatchResult> jobBatchResults = bulkApi.submit(job);
    assertThat(jobBatchResults, hasSize(1));
    assertJobBatchResultError(
        jobBatchResults.get(0), records, "Exception while executing API request!");

    verify(1, postRequestedFor(urlEqualTo(bulkApiEndpoint)));
    verify(1, putRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches")));
    verify(0, patchRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID)));
  }

  @Test
  public void submit_job_should_handle_4xx_error_during_job_close() {
    RestApiError restApiError =
        new RestApiError("The requested resource does not exist", "NOT_FOUND");
    List<Record> records = createRecordsUpToSizeInBytes(ONE_MB);
    Job job = bulkApi.newJobBuilder("Account", Operation.UPSERT, records).build();

    stubCreateBulkApiRequest(job);
    stubUploadBulkApiRequest();
    stubCloseBulkApiRequest(
        responseDefinitionBuilder ->
            responseDefinitionBuilder.withStatus(400).withBody(withErrorResponse(restApiError)));

    List<JobBatchResult> jobBatchResults = bulkApi.submit(job);
    assertThat(jobBatchResults, hasSize(1));
    assertJobBatchResultError(
        jobBatchResults.get(0),
        records,
        "One or more API errors occurred:\n\nCode: NOT_FOUND\nMessage: The requested resource does not exist\n");

    verify(1, postRequestedFor(urlEqualTo(bulkApiEndpoint)));
    verify(1, putRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches")));
    verify(1, patchRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID)));
  }

  @Test
  public void submit_job_should_handle_5xx_error_during_job_close() {
    List<Record> records = createRecordsUpToSizeInBytes(ONE_MB);
    Job job = bulkApi.newJobBuilder("Account", Operation.UPSERT, records).build();

    stubCreateBulkApiRequest(job);
    stubUploadBulkApiRequest();
    stubCloseBulkApiRequest(responseDefinitionBuilder -> responseDefinitionBuilder.withStatus(500));

    List<JobBatchResult> jobBatchResults = bulkApi.submit(job);
    assertThat(jobBatchResults, hasSize(1));
    assertJobBatchResultError(
        jobBatchResults.get(0), records, "Exception while executing API request!");

    verify(1, postRequestedFor(urlEqualTo(bulkApiEndpoint)));
    verify(1, putRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches")));
    verify(1, patchRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID)));
  }

  @Test
  public void submit_job_with_multiple_batches_handles_partial_failure() {
    List<Record> records = createRecordsUpToSizeInBytes(SIZE_100_MB + SIZE_100_MB + ONE_MB);
    int startOfSecondBatch = 99_999;
    int endOfSecondBatch = startOfSecondBatch * 2;
    List<Record> unsubmittedRecords = records.subList(startOfSecondBatch, endOfSecondBatch);
    Job job = bulkApi.newJobBuilder("Account", Operation.HARD_DELETE, records).build();

    stubCreateBulkApiRequest(job);
    stubUploadBulkApiRequest();
    stubCloseBulkApiRequest(
        responseDefinitionBuilder ->
            responseDefinitionBuilder
                .withTransformers("multipleStatuses")
                .withTransformerParameter("statuses", Arrays.asList(200, 500, 200))
                .withBody(
                    jobInfoToJson(
                        createJobInfoBuilder()
                            .withId(TEST_JOB_ID)
                            .withJobState(JobState.UPLOAD_COMPLETE)
                            .build())));

    List<JobBatchResult> jobBatchResults = bulkApi.submit(job);
    assertThat(jobBatchResults, hasSize(3));
    assertJobBatchResultSuccess(jobBatchResults.get(0));
    assertJobBatchResultError(
        jobBatchResults.get(1), unsubmittedRecords, "Exception while executing API request!");
    assertJobBatchResultSuccess(jobBatchResults.get(2));

    verify(3, postRequestedFor(urlEqualTo(bulkApiEndpoint)));
    verify(3, putRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches")));
    verify(3, patchRequestedFor(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID)));
  }

  private void assertJobBatchResultSuccess(JobBatchResult jobBatchResult) {
    assertThat(jobBatchResult.isSuccess(), equalTo(true));
    assertThat(jobBatchResult.isError(), equalTo(false));
    assertThat(jobBatchResult.getJobId(), equalTo(Optional.of(TEST_JOB_ID)));
    assertThat(jobBatchResult.getError(), equalTo(Optional.empty()));
    assertThat(toList(jobBatchResult.getUnsubmittedRecords()), equalTo(Collections.emptyList()));
  }

  private void assertJobBatchResultError(
      JobBatchResult jobBatchResult,
      Iterable<Record> expectedRecords,
      String expectedErrorMessage) {
    assertThat(jobBatchResult.isSuccess(), equalTo(false));
    assertThat(jobBatchResult.isError(), equalTo(true));
    assertThat(jobBatchResult.getJobId(), equalTo(Optional.empty()));
    assertThat(toList(jobBatchResult.getUnsubmittedRecords()), equalTo(toList(expectedRecords)));
    if (jobBatchResult.getError().isPresent()) {
      Throwable error = jobBatchResult.getError().get();
      assertThat(error, instanceOf(BulkApiException.class));
      assertThat(error.getMessage(), equalTo(expectedErrorMessage));
    } else {
      throw new RuntimeException(
          "Was expecting an error on the jobBatchResult but it was not present");
    }
  }

  private List<Record> createRecordsUpToSizeInBytes(long sizeInBytes) {
    int bytesPerLine = 1000;
    String lineEnding = "\r\n";
    String fieldName =
        IntStream.range(0, bytesPerLine - lineEnding.length())
            .mapToObj((i) -> "h")
            .collect(Collectors.joining());
    String headerRow = fieldName + lineEnding;
    String defaultFieldValue =
        IntStream.range(0, bytesPerLine - lineEnding.length())
            .mapToObj((i) -> "v")
            .collect(Collectors.joining());
    List<Record> records = new ArrayList<>();
    long totalBytes = headerRow.getBytes().length;
    while (totalBytes < sizeInBytes) {
      long bytesLeft = sizeInBytes - totalBytes;
      String fieldValue = defaultFieldValue;
      if (bytesLeft < bytesPerLine) {
        fieldValue =
            LongStream.range(0, bytesLeft - lineEnding.length())
                .mapToObj((i) -> "v")
                .collect(Collectors.joining());
      }
      String csvRow = fieldValue + lineEnding;
      totalBytes += csvRow.getBytes().length;
      records.add(new RecordBuilderImpl("Account").withField(fieldName, fieldValue).build());
    }
    return records;
  }
}
