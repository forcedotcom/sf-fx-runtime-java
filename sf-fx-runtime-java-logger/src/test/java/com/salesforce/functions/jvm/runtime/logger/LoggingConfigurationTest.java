/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.slf4j.event.Level;

public class LoggingConfigurationTest {
  @Test
  public void testLogLevelResolution() {
    Map<String, Level> logLevelByLoggerNames = new HashMap<>();
    logLevelByLoggerNames.put("foo", Level.ERROR);
    logLevelByLoggerNames.put("foo.bar", Level.WARN);
    logLevelByLoggerNames.put("x.y.z", Level.DEBUG);

    LoggingConfiguration configuration =
        new LoggingConfiguration(Level.INFO, logLevelByLoggerNames);

    assertThat(configuration.getLogLevelForLoggerName("foo"), is(Level.ERROR));
    assertThat(configuration.getLogLevelForLoggerName("foo.bar"), is(Level.WARN));
    assertThat(configuration.getLogLevelForLoggerName("x.y.z"), is(Level.DEBUG));
    assertThat(configuration.getLogLevelForLoggerName("foo.bar.baz"), is(Level.WARN));
    assertThat(configuration.getLogLevelForLoggerName("x.y.z.foo.bar"), is(Level.DEBUG));
    assertThat(configuration.getLogLevelForLoggerName("x.y"), is(Level.INFO));
    assertThat(configuration.getLogLevelForLoggerName("com.salesforce"), is(Level.INFO));
    assertThat(configuration.getLogLevelForLoggerName(""), is(Level.INFO));
    assertThat(configuration.getLogLevelForLoggerName("...."), is(Level.INFO));
  }
}
