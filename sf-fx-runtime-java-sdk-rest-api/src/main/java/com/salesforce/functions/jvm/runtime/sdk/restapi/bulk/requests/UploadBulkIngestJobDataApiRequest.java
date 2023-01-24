/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.requests;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.*;
import com.salesforce.functions.jvm.runtime.sdk.restapi.bulk.ModifyBulkIngestJobResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.csv.CsvRequestBody;
import com.salesforce.functions.jvm.runtime.sdk.restapi.csv.CsvTable;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public final class UploadBulkIngestJobDataApiRequest
    implements RestApiRequest<ModifyBulkIngestJobResult, CsvRequestBody, JsonElement> {
  private final String jobId;
  private final CsvTable table;

  public UploadBulkIngestJobDataApiRequest(String jobId, CsvTable table) {
    this.jobId = jobId;
    this.table = table;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.PUT;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "jobs", "ingest", jobId, "batches")
        .build();
  }

  @Override
  public Optional<CsvRequestBody> getBody() {
    return Optional.of(new CsvRequestBody(table));
  }

  @Override
  public ModifyBulkIngestJobResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement body) throws RestApiErrorsException {
    if (statusCode == 201) {
      return new ModifyBulkIngestJobResult(jobId);
    } else {
      throw new RestApiErrorsException(ErrorResponseParser.parse(body));
    }
  }

  @Override
  public JsonElement parseBody(byte[] body) throws BodyParsingException {
    try {
      return new Gson()
          .fromJson(new InputStreamReader(new ByteArrayInputStream(body)), JsonElement.class);
    } catch (JsonSyntaxException e) {
      throw new BodyParsingException(e);
    }
  }
}
