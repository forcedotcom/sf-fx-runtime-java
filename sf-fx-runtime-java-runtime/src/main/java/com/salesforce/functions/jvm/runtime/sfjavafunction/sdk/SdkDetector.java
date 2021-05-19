/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.sdk;

import com.salesforce.functions.jvm.runtime.sfjavafunction.sdk.v1.V1SdkLogic;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SdkDetector {
  private static final Logger LOGGER = LoggerFactory.getLogger(SdkDetector.class);

  public static Optional<SdkLogic> detect(ClassLoader projectClassLoader) throws IOException {
    final Properties properties = new Properties();

    try (final InputStream stream =
        projectClassLoader.getResourceAsStream("sf-fx-sdk-java.properties")) {
      properties.load(stream);
    }

    String sdkVersionString = properties.getProperty("version");

    if (sdkVersionString == null) {
      LOGGER.warn(
          "Required 'version' property in 'sf-fx-sdk-java.properties' (loaded from project class loader) is null. SDK detection will not succeed!");
      return Optional.empty();
    }

    if (sdkVersionString.matches("^1\\.\\d+\\.\\d+(-SNAPSHOT)?$")) {
      return Optional.of(V1SdkLogic.init(projectClassLoader));
    }

    LOGGER.warn(
        "Unexpected value of 'version' property in 'sf-fx-sdk-java.properties' (loaded from project class loader): {}.",
        sdkVersionString);

    return Optional.empty();
  }

  private SdkDetector() {}
}
