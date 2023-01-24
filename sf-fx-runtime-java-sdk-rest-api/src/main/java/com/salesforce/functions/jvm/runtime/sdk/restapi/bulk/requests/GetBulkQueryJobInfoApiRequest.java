/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.requests;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.salesforce.functions.jvm.runtime.sdk.restapi.ErrorResponseParser;
import com.salesforce.functions.jvm.runtime.sdk.restapi.HttpMethod;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.BulkQueryJobInfo;
import com.salesforce.functions.jvm.runtime.sdk.restapi.json.JsonRequestBody;
import com.salesforce.functions.jvm.runtime.sdk.restapi.json.JsonRestApiRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public final class GetBulkQueryJobInfoApiRequest extends JsonRestApiRequest<BulkQueryJobInfo> {
  private final String jobId;

  public GetBulkQueryJobInfoApiRequest(String jobId) {
    this.jobId = jobId;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "jobs", "query", jobId)
        .build();
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    return Optional.empty();
  }

  @Override
  public BulkQueryJobInfo processResponse(
      int statusCode, Map<String, String> headers, JsonElement body) throws RestApiErrorsException {

    if (statusCode == 200) {
      return new Gson().fromJson(body, BulkQueryJobInfo.class);
    } else {
      throw new RestApiErrorsException(ErrorResponseParser.parse(body));
    }
  }
}
