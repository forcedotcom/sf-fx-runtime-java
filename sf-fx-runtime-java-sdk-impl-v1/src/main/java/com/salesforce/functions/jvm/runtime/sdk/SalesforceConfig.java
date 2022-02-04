/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SalesforceConfig {

  private static final String PROJECT_TOML = "project.toml";
  private static final String EMPTY_STRING = "";
  private static final String defaultSalesforceApiVersion = "53.0";
  String schemaVersion;
  String id;
  String type;
  String description;
  String salesforceApiVersion;

  public SalesforceConfig() {}

  public String getSchemaVersion() {
    this.schemaVersion = readSalesforceConfig("schema-version");
    return schemaVersion;
  }

  public String getId() {
    this.id = readSalesforceConfig("id");
    return id;
  }

  public String getDescription() {
    this.description = readSalesforceConfig("description");
    return description;
  }

  public String getType() {
    this.type = readSalesforceConfig("type");
    return type;
  }

  public String getSalesforceApiVersion() {
    // The default version of the Salesforce API is set to 53.0 if it is
    // Not declared in the project.toml
    try {

      this.salesforceApiVersion = readSalesforceConfig("salesforce-api-version");
      if (!salesforceApiVersion.equals(EMPTY_STRING)) {
        if (isSupportedApiVersion(salesforceApiVersion)) {
          return salesforceApiVersion;
        }
      } else {
        System.out.println(
            "DEPRECATION NOTICE: com.salesforce.salesforce-api-version is not defined in project.toml and has been defaulted to '53.0'. This field will be required in a future release.");
        return defaultSalesforceApiVersion;
      }
    } catch (Throwable e) {
      System.out.println("Error getting the Salesforce Api Version config from the project.toml");
      e.printStackTrace();
      System.exit(1);
    }
    return defaultSalesforceApiVersion;
  }

  private String readSalesforceConfig(String config) {
    // This method returns the string value of the config found inside
    // the project.toml file, if it doesn't exit it will return an empty string
    try {
      //      System.out.println("retrieving project.toml");
      File projectTomlFile = new File(PROJECT_TOML);
      System.out.println(projectTomlFile);
      if (projectTomlFile.exists()) {
        InputStream targetStream = new FileInputStream(projectTomlFile);
        Toml toml = new Toml().read(targetStream);
        String projectTomlConfig = toml.getString(config);
        System.out.println("config found is " + projectTomlConfig);
        if (!projectTomlConfig.equals(null)) {
          return projectTomlConfig;
        } else {
          return EMPTY_STRING;
        }
      }
    } catch (Throwable e) {
      System.out.println("Error unable to find the project.toml");
      e.printStackTrace();
      System.exit(1);
    }
    return EMPTY_STRING;
  }

  // needs to return string
  public static boolean isSupportedApiVersion(String salesforceApiVersion) {
    // Checks to see if the salesforceApiVersion found in the project.toml
    // Matches a double number
    try {
      // needs to check if the salesforceApiVersion is less than 53
      double salesforceApiVersionDouble = Double.parseDouble(salesforceApiVersion);
      if (salesforceApiVersionDouble < 53.0) {
        return true;
      }
    } catch (Throwable e) {
      System.out.printf(
          "Salesforce Rest API Version %s is not supported. Please change \\`com.salesforce.salesforce-api-version\\` in project.toml to \"53.0\" or newer.",
          salesforceApiVersion);
      e.printStackTrace();
      System.exit(1);
    }
    return false;
  }
}
