/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

public interface RestApiRequest<T, A extends RestApiRequestBody, B> {
  HttpMethod getHttpMethod();

  URI createUri(URI baseUri, String apiVersion) throws URISyntaxException;

  Optional<A> getBody();

  T processResponse(int statusCode, Map<String, String> headers, B body)
      throws RestApiErrorsException;

  B parseBody(byte[] body) throws BodyParsingException;
}
