/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public interface RestApiRequest<T> {
  HttpMethod getHttpMethod();

  URI createUri(URI baseUri, String apiVersion) throws URISyntaxException;

  default Optional<HttpEntity> getBody() {
    return Optional.empty();
  }

  T processResponse(HttpResponse response)
      throws RestApiErrorsException, IOException, RestApiException;
}
