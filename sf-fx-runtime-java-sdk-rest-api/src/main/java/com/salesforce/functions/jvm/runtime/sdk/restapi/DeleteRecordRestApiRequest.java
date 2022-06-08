/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import static com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiSupport.isOK;
import static com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiSupport.parseJsonErrors;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

public class DeleteRecordRestApiRequest implements RestApiRequest<ModifyRecordResult> {
  private final String type;
  private final String id;

  public DeleteRecordRestApiRequest(String type, String id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.DELETE;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return new URIBuilder(baseUri)
        .setPathSegments("services", "data", "v" + apiVersion, "sobjects", type, id)
        .build();
  }

  @Override
  public ModifyRecordResult processResponse(HttpResponse response)
      throws RestApiErrorsException, IOException, RestApiException {
    if (!isOK(response)) {
      throw parseJsonErrors(response);
    }
    return new ModifyRecordResult(id);
  }
}
