/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import static com.salesforce.functions.jvm.runtime.sdk.bulk.BulkApiSupport.bulkApiGsonBuilder;
import static com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiSupport.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.salesforce.functions.jvm.runtime.sdk.restapi.HttpMethod;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiRequest;
import com.salesforce.functions.jvm.sdk.bulk.JobState;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class BulkApiJobRequestCloseJob implements RestApiRequest<JobInfo> {

  private final String jobId;

  public BulkApiJobRequestCloseJob(String jobId) {
    this.jobId = jobId;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "jobs", "ingest", jobId)
        .build();
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.PATCH;
  }

  @Override
  public Optional<HttpEntity> getBody() {
    JsonObject entityBody = new JsonObject();
    entityBody.addProperty("state", JobState.UPLOAD_COMPLETE.getTextValue());
    return Optional.of(
        new StringEntity(new Gson().toJson(entityBody), ContentType.APPLICATION_JSON));
  }

  @Override
  public JobInfo processResponse(HttpResponse response)
      throws RestApiErrorsException, IOException, RestApiException {
    if (!isOK(response)) {
      throw parseJsonErrors(response);
    }
    return parseJson(response, JobInfo.class, bulkApiGsonBuilder);
  }
}
