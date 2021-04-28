/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.slf4j.event.Level;

public class LoggingConfiguratorTest extends StdOutAndStdErrCapturingTest {

  @Test
  public void testRootLevelConfiguration_DEBUG() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "DEBUG");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.DEBUG));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testRootLevelConfiguration_INFO() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "INFO");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.INFO));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testRootLevelConfiguration_WARN() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "WARN");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.WARN));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testRootLevelConfiguration_TRACE() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "TRACE");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.TRACE));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testRootLevelConfiguration_ERROR() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "ERROR");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.ERROR));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testRootLevelConfiguration_DEBUg() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "DEBUg");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.DEBUG));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testRootLevelConfiguration_info() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "info");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.INFO));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testRootLevelConfiguration_WArN() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "WArN");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.WARN));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testRootLevelConfiguration_TRAcE() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "TRAcE");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.TRACE));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testRootLevelConfiguration_Error() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "Error");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.ERROR));
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testInvalidRootLevelConfiguration() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL", "debugg");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);

    assertThat(configuration.getRootLogLevel(), is(Level.INFO));

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(
        systemErrContent.toString(),
        is(
            equalTo(
                "WARNING: Environment variable 'SF_FX_LOGLEVEL' contains unknown log level 'debugg'.\nWARNING: Environment variable 'SF_FX_LOGLEVEL' will be ignored!\n")));
  }

  @Test
  public void testLoggerSpecificLevelConfiguration() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL_com_salesforce", "debug");
    environment.put("SF_FX_LOGLEVEL_com_salesforce_functions_jvm_internal", "WARN");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);
    assertThat(configuration.getLogLevelForLoggerName("com.salesforce"), is(Level.DEBUG));
    assertThat(configuration.getLogLevelForLoggerName("com.salesforce.functions"), is(Level.DEBUG));
    assertThat(
        configuration.getLogLevelForLoggerName("com.salesforce.functions.jvm"), is(Level.DEBUG));
    assertThat(
        configuration.getLogLevelForLoggerName("com.salesforce.functions.jvm.internal"),
        is(Level.WARN));

    assertThat(configuration.getRootLogLevel(), is(Level.INFO));

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testInvalidLoggerSpecificLevelConfiguration() {
    Map<String, String> environment = new HashMap<>();
    environment.put("SF_FX_LOGLEVEL_com_salesforce", "debug");
    environment.put("SF_FX_LOGLEVEL_com_salesforce_functions_jvm_internal", "HORSE");

    LoggingConfiguration configuration = LoggingConfigurator.configureFromEnvironment(environment);
    assertThat(configuration.getLogLevelForLoggerName("com.salesforce"), is(Level.DEBUG));
    assertThat(configuration.getLogLevelForLoggerName("com.salesforce.functions"), is(Level.DEBUG));
    assertThat(
        configuration.getLogLevelForLoggerName("com.salesforce.functions.jvm"), is(Level.DEBUG));
    assertThat(
        configuration.getLogLevelForLoggerName("com.salesforce.functions.jvm.internal"),
        is(Level.DEBUG));

    assertThat(configuration.getRootLogLevel(), is(Level.INFO));

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(
        systemErrContent.toString(),
        is(
            equalTo(
                "WARNING: Environment variable 'SF_FX_LOGLEVEL_com_salesforce_functions_jvm_internal' contains unknown log level 'HORSE'.\nWARNING: Environment variable 'SF_FX_LOGLEVEL_com_salesforce_functions_jvm_internal' will be ignored!\n")));
  }
}
