/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.moandjiezana.toml.Toml;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import java.io.InputStream;

public class SalesforceConfigImpl extends SalesforceContextCloudEventExtension {

  private static String schemaVersion = "schema-version";
  private static String id = "id";
  private static String description = "description";
  private static String type = "type";
  private static String salesforceApiVersion = "salesforce-api-version";

  private static final String PROJECT_TOML = "project.toml";
  private static final String EMPTY_STRING = "";
  private final String defaultSalesforceApiVersion = "53.0";

  public String getSchemaVersion() {
    return schemaVersion;
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public String getType() {
    return type;
  }

  public String getSalesforceApiVersion() {
    // The default version of the Salesforce API is set to 53.0 if it is
    // Not declared in the project.toml
    try {
      readProjectTomlFile(salesforceApiVersion);
      if (!salesforceApiVersion.equals(EMPTY_STRING)) {
        isSupportedApiVersion(salesforceApiVersion);
        return salesforceApiVersion;
      } else {
        System.out.println(
            "DEPRECATION NOTICE: com.salesforce.salesforce-api-version is not defined in project.toml and has been defaulted to '53.0'. This field will be required in a future release.");
        return defaultSalesforceApiVersion;
      }
    } catch (Throwable e) {
    }
    return defaultSalesforceApiVersion;
  }

  private String readProjectTomlFile(String config) {

    InputStream stream =
        SalesforceConfigImpl.class.getClassLoader().getResourceAsStream(PROJECT_TOML);
    Toml toml = new Toml().read(stream);
    String projectTomlConfig = toml.getString(config);
    if (!projectTomlConfig.isEmpty()) {
      return projectTomlConfig;
    } else {
      return "";
    }
  }

  public static void isSupportedApiVersion(String salesforceApiVersion) {
    // Checks to see if the salesforceApiVersion found in the project.toml
    // Matches a double number
    try {
      salesforceApiVersion.matches("[0-9]{1,13}(\\.[0-9]*)?");
    } catch (Throwable e) {
      System.out.printf(
          "Salesforce Rest API Version %s is not supported. Please change \\`com.salesforce.salesforce-api-version\\` in project.toml to \"53.0\" or newer.",
          salesforceApiVersion);
    }
  }
}
