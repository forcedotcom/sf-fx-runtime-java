/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.requests;

import com.salesforce.functions.jvm.runtime.sdk.restapi.HttpMethod;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.BulkQueryResults;
import com.salesforce.functions.jvm.runtime.sdk.restapi.csv.CsvResponseRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.csv.CsvTable;
import com.salesforce.functions.jvm.runtime.sdk.restapi.json.JsonRequestBody;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.http.client.utils.URIBuilder;

public final class GetBulkQueryJobResultsApiRequest
    extends CsvResponseRestApiRequest<BulkQueryResults, JsonRequestBody> {
  private final String jobId;
  private final String locator;
  private final Long maxRecords;

  public GetBulkQueryJobResultsApiRequest(
      String jobId, @Nullable String locator, @Nullable Long maxRecords) {
    this.jobId = jobId;
    this.locator = locator;
    this.maxRecords = maxRecords;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    URIBuilder uriBuilder =
        new URIBuilder(baseUri)
            .setPathSegments(
                "services", "data", "v" + apiVersion, "jobs", "query", jobId, "results");

    if (locator != null) {
      uriBuilder.addParameter("locator", locator);
    }

    if (maxRecords != null) {
      uriBuilder.addParameter("maxRecords", maxRecords.toString());
    }

    return uriBuilder.build();
  }

  @Override
  public BulkQueryResults processResponse(
      int statusCode, Map<String, String> headers, CsvTable body) throws RestApiErrorsException {
    if (statusCode == 200) {
      // The API will always send a locator header, even if there is no locator. In such cases, the
      // value will be the "null" as a string.
      String locator = headers.get("Sforce-Locator");
      if (locator.equals("null")) {
        locator = null;
      }

      return new BulkQueryResults(locator, body);
    }

    throw new RestApiErrorsException(Collections.emptyList());
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    return Optional.empty();
  }
}
