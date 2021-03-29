/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.cloudevent;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public final class SalesforceCloudEventExtensionParser {
  private static final Gson gson = new Gson();

  public static Optional<SalesforceContextCloudEventExtension> parseSalesforceContext(
      CloudEvent cloudEvent) {
    return parseBase64JsonExtension(
        cloudEvent, "sfcontext", SalesforceContextCloudEventExtension.class);
  }

  public static Optional<SalesforceFunctionContextCloudEventExtension>
      parseSalesforceFunctionContext(CloudEvent cloudEvent) {
    return parseBase64JsonExtension(
        cloudEvent, "sffncontext", SalesforceFunctionContextCloudEventExtension.class);
  }

  private static <A> Optional<A> parseBase64JsonExtension(
      CloudEvent cloudEvent, String extensionName, Class<A> clazz) {
    Object sfContextExtensionObject = cloudEvent.getExtension(extensionName);
    if (!(sfContextExtensionObject instanceof String)) {
      return Optional.empty();
    }

    byte[] base64DecodedExtension = Base64.getDecoder().decode((String) sfContextExtensionObject);
    String extensionString = new String(base64DecodedExtension, StandardCharsets.UTF_8);

    try {
      return Optional.ofNullable(gson.fromJson(extensionString, clazz));
    } catch (JsonSyntaxException e) {
      return Optional.empty();
    }
  }
}
