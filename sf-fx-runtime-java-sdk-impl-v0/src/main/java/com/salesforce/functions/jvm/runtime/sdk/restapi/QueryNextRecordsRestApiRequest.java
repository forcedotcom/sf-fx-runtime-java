/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.JsonElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public class QueryNextRecordsRestApiRequest extends AbstractQueryRestApiRequest {
  private final String nextRecordsPath;

  public QueryNextRecordsRestApiRequest(String nextRecordsPath) {
    this.nextRecordsPath = nextRecordsPath;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri).setPath(nextRecordsPath).build();
  }

  @Override
  public Optional<JsonElement> getBody() {
    return Optional.empty();
  }
}
