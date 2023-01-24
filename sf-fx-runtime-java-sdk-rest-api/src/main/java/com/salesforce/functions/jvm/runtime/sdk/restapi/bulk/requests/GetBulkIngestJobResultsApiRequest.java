/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.requests;

import com.salesforce.functions.jvm.runtime.sdk.restapi.HttpMethod;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.csv.CsvResponseRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.csv.CsvTable;
import com.salesforce.functions.jvm.runtime.sdk.restapi.json.JsonRequestBody;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public final class GetBulkIngestJobResultsApiRequest
    extends CsvResponseRestApiRequest<CsvTable, JsonRequestBody> {
  private final String jobId;
  private final RecordStatus status;

  public GetBulkIngestJobResultsApiRequest(String jobId, RecordStatus status) {
    this.jobId = jobId;
    this.status = status;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    String statusString;
    switch (status) {
      case FAILED:
        statusString = "failedResults";
        break;
      case SUCCESSFUL:
        statusString = "successfulResults";
        break;
      case UNPROCESSED:
        statusString = "unprocessedrecords";
        break;
      default:
        throw new IllegalStateException("Missing URL mapping for job status: " + status);
    }

    return new URIBuilder(baseUri)
        .setPathSegments(
            "services", "data", "v" + apiVersion, "jobs", "ingest", jobId, statusString)
        .build();
  }

  @Override
  public CsvTable processResponse(int statusCode, Map<String, String> headers, CsvTable body)
      throws RestApiErrorsException {
    if (statusCode == 200) {
      return body;
    }

    throw new RestApiErrorsException(Collections.emptyList());
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    return Optional.empty();
  }

  public enum RecordStatus {
    UNPROCESSED,
    FAILED,
    SUCCESSFUL
  }
}
