/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiSupport.isOK;
import static com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiSupport.parseJsonErrors;

import com.salesforce.functions.jvm.runtime.sdk.restapi.HttpMethod;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiRequest;
import com.salesforce.functions.jvm.sdk.Record;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class BulkApiRequestUploadJobData implements RestApiRequest<Void> {
  private final List<Record> records;
  private final String jobId;

  public BulkApiRequestUploadJobData(String jobId, List<Record> records) {
    this.records = records;
    this.jobId = jobId;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "jobs", "ingest", jobId, "batches")
        .build();
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.PUT;
  }

  @Override
  public Optional<HttpEntity> getBody() {
    if (records.isEmpty()) {
      return Optional.empty();
    }

    try {
      StringWriter stringWriter = new StringWriter();
      String[] fieldNames = records.get(0).getFieldNames().stream().sorted().toArray(String[]::new);
      CSVPrinter csvPrinter =
          CSVFormat.RFC4180.builder().setHeader(fieldNames).build().print(stringWriter);
      for (Record record : records) {
        List<Object> values =
            Arrays.stream(fieldNames)
                .sequential()
                .map(fieldName -> record.getStringField(fieldName).orElse(""))
                .collect(Collectors.toList());
        csvPrinter.printRecord(values);
      }

      return Optional.of(new StringEntity(stringWriter.toString(), ContentType.create("text/csv")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Void processResponse(HttpResponse response)
      throws RestApiException, RestApiErrorsException {
    if (!isOK(response)) {
      throw parseJsonErrors(response);
    }
    return null;
  }
}
