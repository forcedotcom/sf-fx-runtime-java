/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.logger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

public class LoggingConfigurationTest {
    @Test
    public void testLogLevelResolution() {
        Map<String, Level> logLevelByLoggerNames = new HashMap<>();
        logLevelByLoggerNames.put("foo", Level.ERROR);
        logLevelByLoggerNames.put("foo.bar", Level.WARN);
        logLevelByLoggerNames.put("x.y.z", Level.DEBUG);

        LoggingConfiguration configuration = new LoggingConfiguration(Level.INFO, logLevelByLoggerNames);

        Map<String, Level> expectedLogLevels = new HashMap<>();
        expectedLogLevels.put("foo", Level.ERROR);
        expectedLogLevels.put("foo.bar", Level.WARN);
        expectedLogLevels.put("x.y.z", Level.DEBUG);

        expectedLogLevels.put("foo.bar.baz", Level.WARN);
        expectedLogLevels.put("x.y.z.foo.bar", Level.DEBUG);

        expectedLogLevels.put("x.y", Level.INFO);
        expectedLogLevels.put("com.salesforce", Level.INFO);

        expectedLogLevels.put("", Level.INFO);
        expectedLogLevels.put("....", Level.INFO);

        for (Map.Entry<String, Level> entry : expectedLogLevels.entrySet()) {
            Assert.assertEquals(entry.getValue(), configuration.getLogLevelForLoggerName(entry.getKey()));
        }
    }
}

