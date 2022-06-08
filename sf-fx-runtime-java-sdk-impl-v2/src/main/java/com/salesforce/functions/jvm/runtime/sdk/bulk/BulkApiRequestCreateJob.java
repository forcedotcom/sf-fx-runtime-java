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
import com.salesforce.functions.jvm.sdk.bulk.Operation;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class BulkApiRequestCreateJob implements RestApiRequest<JobInfo> {
  private final String objectType;
  private final Operation operation;

  public BulkApiRequestCreateJob(@Nonnull String objectType, @Nonnull Operation operation) {
    this.objectType = objectType;
    this.operation = operation;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.POST;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "jobs", "ingest")
        .build();
  }

  @Override
  public Optional<HttpEntity> getBody() {
    JsonObject entityBody = new JsonObject();
    entityBody.addProperty("object", objectType);
    entityBody.addProperty("operation", operation.getTextValue());
    entityBody.addProperty("contentType", "CSV");
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
