/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.event.Level;

public class LoggingConfiguratorTest {
  private final PrintStream previousSystemOut = System.out;
  private final PrintStream previousSystemErr = System.err;
  private final ByteArrayOutputStream systemOutContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream systemErrContent = new ByteArrayOutputStream();

  @Before
  public void redirectOutputStreams() {
    System.setOut(new PrintStream(systemOutContent));
    System.setErr(new PrintStream(systemErrContent));
  }

  @After
  public void restoreOutputStreams() {
    System.setOut(previousSystemOut);
    System.setErr(previousSystemErr);
  }

  @Test
  public void testRootLevelConfiguration() {
    Map<String, Level> expectations = new HashMap<>();
    expectations.put("DEBUG", Level.DEBUG);
    expectations.put("INFO", Level.INFO);
    expectations.put("WARN", Level.WARN);
    expectations.put("TRACE", Level.TRACE);
    expectations.put("ERROR", Level.ERROR);

    expectations.put("DEBUg", Level.DEBUG);
    expectations.put("info", Level.INFO);
    expectations.put("WArN", Level.WARN);
    expectations.put("TRAcE", Level.TRACE);
    expectations.put("Error", Level.ERROR);

    for (Map.Entry<String, Level> entry : expectations.entrySet()) {
      Map<String, String> environment = new HashMap<>();
      environment.put("SF_FX_LOGLEVEL", entry.getKey());

      LoggingConfiguration configuration =
          LoggingConfigurator.configureFromEnvironment(environment);
      Assert.assertEquals(entry.getValue(), configuration.getRootLogLevel());
    }

    Assert.assertEquals("", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testInvalidRootLevelConfiguration() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "debugg");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);
    Assert.assertEquals(Level.INFO, configuration.getRootLogLevel());
    Assert.assertEquals("", systemOutContent.toString());
    Assert.assertEquals(
        "WARNING: Environment variable 'SF_FX_LOGLEVEL' contains unknown log level 'debugg'.\nWARNING: Environment variable 'SF_FX_LOGLEVEL' will be ignored!\n",
        systemErrContent.toString());
  }

  @Test
  public void testLoggerSpecificLevelConfiguration() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL_com_salesforce", "debug");
    environment.put("SF_FX_LOGLEVEL_com_salesforce_functions_jvm_internal", "WARN");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    Assert.assertEquals(Level.DEBUG, configuration.getLogLevelForLoggerName("com.salesforce"));
    Assert.assertEquals(
        Level.DEBUG, configuration.getLogLevelForLoggerName("com.salesforce.functions"));
    Assert.assertEquals(
        Level.DEBUG, configuration.getLogLevelForLoggerName("com.salesforce.functions.jvm"));
    Assert.assertEquals(
        Level.WARN,
        configuration.getLogLevelForLoggerName("com.salesforce.functions.jvm.internal"));

    Assert.assertEquals(Level.INFO, configuration.getRootLogLevel());
    Assert.assertEquals("", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testInvalidLoggerSpecificLevelConfiguration() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL_com_salesforce", "debug");
    environment.put("SF_FX_LOGLEVEL_com_salesforce_functions_jvm_internal", "HORSE");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    Assert.assertEquals(Level.DEBUG, configuration.getLogLevelForLoggerName("com.salesforce"));
    Assert.assertEquals(
        Level.DEBUG, configuration.getLogLevelForLoggerName("com.salesforce.functions"));
    Assert.assertEquals(
        Level.DEBUG, configuration.getLogLevelForLoggerName("com.salesforce.functions.jvm"));
    Assert.assertEquals(
        Level.DEBUG,
        configuration.getLogLevelForLoggerName("com.salesforce.functions.jvm.internal"));

    Assert.assertEquals(Level.INFO, configuration.getRootLogLevel());
    Assert.assertEquals("", systemOutContent.toString());
    Assert.assertEquals(
        "WARNING: Environment variable 'SF_FX_LOGLEVEL_com_salesforce_functions_jvm_internal' contains unknown log level 'HORSE'.\nWARNING: Environment variable 'SF_FX_LOGLEVEL_com_salesforce_functions_jvm_internal' will be ignored!\n",
        systemErrContent.toString());
  }
}
