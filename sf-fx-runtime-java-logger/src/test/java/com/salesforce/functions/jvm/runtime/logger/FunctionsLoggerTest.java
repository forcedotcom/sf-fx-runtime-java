/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.event.Level;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class FunctionsLoggerTest {
    private final PrintStream previousSystemOut = System.out;
    private final ByteArrayOutputStream systemOutContent = new ByteArrayOutputStream();

    private static final class TestingLoggingFormatter implements LoggingFormatter {
        @Override
        public String format(String loggerName, Level level, String message) {
            return String.format("%s %s - %s", loggerName, level.toString(), message);
        }
    }

    private final FunctionsLogger fooBarInfoLogger = new FunctionsLogger("foo.bar", Level.INFO, new TestingLoggingFormatter());
    private final FunctionsLogger barBazDebugLogger = new FunctionsLogger("bar.baz", Level.DEBUG, new TestingLoggingFormatter());

    @Before
    public void redirectOutputStreams() {
        System.setOut(new PrintStream(systemOutContent));
    }

    @After
    public void restoreOutputStreams() {
        System.setOut(previousSystemOut);
    }

    @Test
    public void testIgnoredLogLevel_1() {
        fooBarInfoLogger.debug("Hello World!");
        Assert.assertEquals("", systemOutContent.toString());
    }

    @Test
    public void testIgnoredLogLevel_2() {
        fooBarInfoLogger.trace("Hello World!");
        Assert.assertEquals("", systemOutContent.toString());
    }

    @Test
    public void testIgnoredLogLevel_3() {
        barBazDebugLogger.trace("Hello World!");
        Assert.assertEquals("", systemOutContent.toString());
    }

    @Test
    public void testExactLogLevel() {
        fooBarInfoLogger.info("Hello exact world!");
        Assert.assertEquals("foo.bar INFO - Hello exact world!", systemOutContent.toString());
    }

    @Test
    public void testHigherLogLevel_1() {
        fooBarInfoLogger.warn("Hello higher world!");
        Assert.assertEquals("foo.bar WARN - Hello higher world!", systemOutContent.toString());
    }

    @Test
    public void testHigherLogLevel_2() {
        fooBarInfoLogger.error("Hello higher world!");
        Assert.assertEquals("foo.bar ERROR - Hello higher world!", systemOutContent.toString());
    }

    @Test
    public void testFormattedMessage_1() {
        barBazDebugLogger.debug("Hello {}!", "World");
        Assert.assertEquals("bar.baz DEBUG - Hello World!", systemOutContent.toString());
    }

    @Test
    public void testFormattedMessage_2() {
        barBazDebugLogger.debug("{} {}!", 23, "people");
        Assert.assertEquals("bar.baz DEBUG - 23 people!", systemOutContent.toString());
    }

    @Test
    public void testFormattedMessage_3() {
        fooBarInfoLogger.warn("{} {} {} {} {} {}...", 4, 8, 15, 16, 23, 42);
        Assert.assertEquals("foo.bar WARN - 4 8 15 16 23 42...", systemOutContent.toString());
    }

    @Test
    public void testFormattedMessage_4() {
        class Foo {
            @Override
            public String toString() {
                return "Foo {internal=bar}";
            }
        }

        fooBarInfoLogger.error("Result: {}", new Foo());
        Assert.assertEquals("foo.bar ERROR - Result: Foo {internal=bar}", systemOutContent.toString());
    }
}