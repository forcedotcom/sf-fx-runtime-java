/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.salesforce.functions.jvm.runtime.sdk.bulk.BulkApiSupport.bulkApiGsonBuilder;
import static com.salesforce.functions.jvm.runtime.sdk.bulk.JobInfoBuilder.createJobInfoBuilder;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApi;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiError;
import com.salesforce.functions.jvm.sdk.Record;
import com.salesforce.functions.jvm.sdk.bulk.Job;
import com.salesforce.functions.jvm.sdk.bulk.JobState;
import com.salesforce.functions.jvm.sdk.bulk.Operation;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
abstract class BulkApiTestCase {
  private static final URI API_ENDPOINT = URI.create("http://localhost:8080");
  private static final String API_VERSION = "53.0";
  private static final String ACCESS_TOKEN = "testAccessToken";

  protected static final String bulkApiEndpoint =
      String.format("/services/data/v%s/jobs/ingest", API_VERSION);

  protected static final String TEST_JOB_ID = "testJobId";

  @SuppressWarnings("unchecked")
  @Rule
  public WireMockRule wireMock =
      new WireMockRule(
          options()
              .extensions(MultipleStatusesTransformer.class)
              .notifier(new ConsoleNotifier(false)));

  protected RestApi api;

  @Before
  public void setUp() {
    api = new RestApi(API_ENDPOINT, API_VERSION, ACCESS_TOKEN);
  }

  protected void stubCreateBulkApiRequest(Job job) {
    stubCreateBulkApiRequest(job.getObjectType(), job.getOperation());
  }

  protected void stubCreateBulkApiRequest(String objectType, Operation operation) {
    stubCreateBulkApiRequest(
        objectType,
        operation,
        (jobInfo, responseBuilder) ->
            responseBuilder.withStatus(200).withBody(jobInfoToJson(jobInfo)));
  }

  protected void stubCreateBulkApiRequest(
      Job job, BiFunction<JobInfo, ResponseDefinitionBuilder, ResponseDefinitionBuilder> response) {
    stubCreateBulkApiRequest(job.getObjectType(), job.getOperation(), response);
  }

  protected void stubCreateBulkApiRequest(
      String objectType,
      Operation operation,
      BiFunction<JobInfo, ResponseDefinitionBuilder, ResponseDefinitionBuilder> response) {
    JobInfo jobInfo =
        createJobInfoBuilder()
            .withId(TEST_JOB_ID)
            .withObjectType(objectType)
            .withOperation(operation)
            .build();
    ResponseDefinitionBuilder responseBuilder =
        aResponse().withHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
    stubFor(post(urlEqualTo(bulkApiEndpoint)).willReturn(response.apply(jobInfo, responseBuilder)));
  }

  protected void stubUploadBulkApiRequest() {
    stubUploadBulkApiRequest(responseBuilder -> responseBuilder.withStatus(201));
  }

  protected void stubUploadBulkApiRequest(
      Function<ResponseDefinitionBuilder, ResponseDefinitionBuilder> response) {
    stubFor(
        put(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID + "/batches"))
            .willReturn(response.apply(aResponse())));
  }

  protected void stubCloseBulkApiRequest() {
    JobInfo responseBody =
        createJobInfoBuilder().withId(TEST_JOB_ID).withJobState(JobState.UPLOAD_COMPLETE).build();

    stubCloseBulkApiRequest(
        (responseBuilder) -> responseBuilder.withStatus(200).withBody(jobInfoToJson(responseBody)));
  }

  protected void stubCloseBulkApiRequest(
      Function<ResponseDefinitionBuilder, ResponseDefinitionBuilder> response) {
    JobInfo requestBody = createJobInfoBuilder().withJobState(JobState.UPLOAD_COMPLETE).build();
    ResponseDefinitionBuilder responseBuilder =
        aResponse().withHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
    stubFor(
        patch(urlEqualTo(bulkApiEndpoint + "/" + TEST_JOB_ID))
            .withRequestBody(equalToJson(jobInfoToJson(requestBody)))
            .willReturn(response.apply(responseBuilder)));
  }

  protected List<Record> toList(Iterable<Record> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
  }

  protected String jobInfoToJson(JobInfo jobInfo) {
    Gson gson = bulkApiGsonBuilder.apply(new GsonBuilder()).create();
    return gson.toJson(jobInfo);
  }

  protected String withErrorResponse(RestApiError... restApiErrors) {
    return new Gson().toJson(restApiErrors);
  }

  public static class MultipleStatusesTransformer extends ResponseDefinitionTransformer {
    private int index = 0;

    @Override
    @SuppressWarnings("unchecked")
    public ResponseDefinition transform(
        Request request,
        ResponseDefinition responseDefinition,
        FileSource fileSource,
        Parameters parameters) {
      List<Integer> statuses = (List<Integer>) parameters.get("statuses");
      int status = statuses.get(index);
      index += 1;
      ResponseDefinitionBuilder responseDefinitionBuilder =
          new ResponseDefinitionBuilder()
              .withHeaders(responseDefinition.getHeaders())
              .withStatus(status);
      if (status < 500) {
        responseDefinitionBuilder.withBody(responseDefinition.getBody());
      }
      return responseDefinitionBuilder.build();
    }

    @Override
    public String getName() {
      return "multipleStatuses";
    }

    @Override
    public boolean applyGlobally() {
      return false;
    }
  }
}
