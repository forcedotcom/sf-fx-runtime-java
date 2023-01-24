/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.BodyParsingException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public abstract class JsonRestApiRequest<T>
    implements RestApiRequest<T, JsonRequestBody, JsonElement> {
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
