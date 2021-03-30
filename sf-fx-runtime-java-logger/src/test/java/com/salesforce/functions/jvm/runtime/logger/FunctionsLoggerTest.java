/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

public class FunctionsLoggerTest {
  private final PrintStream previousSystemOut = System.out;
  private final PrintStream previousSystemErr = System.err;
  private final ByteArrayOutputStream systemOutContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream systemErrContent = new ByteArrayOutputStream();

  private static final class TestingLoggingFormatter implements LoggingFormatter {
    @Override
    public String format(String loggerName, Level level, String message) {
      return String.format("%s %s - %s", loggerName, level.toString(), message);
    }
  }

  private final FunctionsLogger traceLogger =
      new FunctionsLogger("traceLogger", Level.TRACE, new TestingLoggingFormatter());
  private final FunctionsLogger fooBarInfoLogger =
      new FunctionsLogger("foo.bar", Level.INFO, new TestingLoggingFormatter());
  private final FunctionsLogger barBazDebugLogger =
      new FunctionsLogger("bar.baz", Level.DEBUG, new TestingLoggingFormatter());

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
  public void setGetName() {
    Assert.assertEquals("foo.bar", fooBarInfoLogger.getName());
    Assert.assertEquals("bar.baz", barBazDebugLogger.getName());
    Assert.assertEquals("traceLogger", traceLogger.getName());
  }

  @Test
  public void testIsTraceEnabled() {
    Assert.assertTrue(traceLogger.isTraceEnabled());
    Assert.assertFalse(fooBarInfoLogger.isTraceEnabled());
    Assert.assertFalse(barBazDebugLogger.isTraceEnabled());

    Assert.assertTrue(traceLogger.isTraceEnabled(TEST_MARKER));
    Assert.assertFalse(fooBarInfoLogger.isTraceEnabled(TEST_MARKER));
    Assert.assertFalse(barBazDebugLogger.isTraceEnabled(TEST_MARKER));
  }

  @Test
  public void testIsDebugEnabled() {
    Assert.assertTrue(traceLogger.isDebugEnabled());
    Assert.assertFalse(fooBarInfoLogger.isDebugEnabled());
    Assert.assertTrue(barBazDebugLogger.isDebugEnabled());

    Assert.assertTrue(traceLogger.isDebugEnabled(TEST_MARKER));
    Assert.assertFalse(fooBarInfoLogger.isDebugEnabled(TEST_MARKER));
    Assert.assertTrue(barBazDebugLogger.isDebugEnabled(TEST_MARKER));
  }

  @Test
  public void testIsInfoEnabled() {
    Assert.assertTrue(traceLogger.isInfoEnabled());
    Assert.assertTrue(fooBarInfoLogger.isInfoEnabled());
    Assert.assertTrue(barBazDebugLogger.isInfoEnabled());

    Assert.assertTrue(traceLogger.isInfoEnabled(TEST_MARKER));
    Assert.assertTrue(fooBarInfoLogger.isInfoEnabled(TEST_MARKER));
    Assert.assertTrue(barBazDebugLogger.isInfoEnabled(TEST_MARKER));
  }

  @Test
  public void testIsWarnEnabled() {
    Assert.assertTrue(traceLogger.isWarnEnabled());
    Assert.assertTrue(fooBarInfoLogger.isWarnEnabled());
    Assert.assertTrue(barBazDebugLogger.isWarnEnabled());

    Assert.assertTrue(traceLogger.isWarnEnabled(TEST_MARKER));
    Assert.assertTrue(fooBarInfoLogger.isWarnEnabled(TEST_MARKER));
    Assert.assertTrue(barBazDebugLogger.isWarnEnabled(TEST_MARKER));
  }

  @Test
  public void testIsErrorEnabled() {
    Assert.assertTrue(traceLogger.isErrorEnabled());
    Assert.assertTrue(fooBarInfoLogger.isErrorEnabled());
    Assert.assertTrue(barBazDebugLogger.isErrorEnabled());

    Assert.assertTrue(traceLogger.isErrorEnabled(TEST_MARKER));
    Assert.assertTrue(fooBarInfoLogger.isErrorEnabled(TEST_MARKER));
    Assert.assertTrue(barBazDebugLogger.isErrorEnabled(TEST_MARKER));
  }

  @Test
  public void testIgnoredLogLevel_1() {
    fooBarInfoLogger.debug("Hello World!");
    fooBarInfoLogger.debug("Hello World! {}", "x");
    fooBarInfoLogger.debug("Hello World! {} {}", "x", "y");
    fooBarInfoLogger.debug("Hello World! {} {} {}", "x", "y", "z");
    fooBarInfoLogger.debug("Hello World!", TEST_EXCEPTION);

    fooBarInfoLogger.debug(TEST_MARKER, "Hello World!");
    fooBarInfoLogger.debug(TEST_MARKER, "Hello World! {}", "x");
    fooBarInfoLogger.debug(TEST_MARKER, "Hello World! {} {}", "x", "y");
    fooBarInfoLogger.debug(TEST_MARKER, "Hello World! {} {} {}", "x", "y", "z");
    fooBarInfoLogger.debug(TEST_MARKER, "Hello World!", TEST_EXCEPTION);

    Assert.assertEquals("", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testIgnoredLogLevel_2() {
    fooBarInfoLogger.trace("Hello World!");
    fooBarInfoLogger.trace("Hello World! {}", "x");
    fooBarInfoLogger.trace("Hello World! {} {}", "x", "y");
    fooBarInfoLogger.trace("Hello World! {} {} {}", "x", "y", "z");
    fooBarInfoLogger.trace("Hello World!", TEST_EXCEPTION);

    fooBarInfoLogger.trace(TEST_MARKER, "Hello World!");
    fooBarInfoLogger.trace(TEST_MARKER, "Hello World! {}", "x");
    fooBarInfoLogger.trace(TEST_MARKER, "Hello World! {} {}", "x", "y");
    fooBarInfoLogger.trace(TEST_MARKER, "Hello World! {} {} {}", "x", "y", "z");
    fooBarInfoLogger.trace(TEST_MARKER, "Hello World!", TEST_EXCEPTION);

    Assert.assertEquals("", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testIgnoredLogLevel_3() {
    barBazDebugLogger.trace("Hello World!");
    barBazDebugLogger.trace("Hello World! {}", "x");
    barBazDebugLogger.trace("Hello World! {} {}", "x", "y");
    barBazDebugLogger.trace("Hello World! {} {} {}", "x", "y", "z");
    barBazDebugLogger.trace("Hello World!", TEST_EXCEPTION);

    barBazDebugLogger.trace(TEST_MARKER, "Hello World!");
    barBazDebugLogger.trace(TEST_MARKER, "Hello World! {}", "x");
    barBazDebugLogger.trace(TEST_MARKER, "Hello World! {} {}", "x", "y");
    barBazDebugLogger.trace(TEST_MARKER, "Hello World! {} {} {}", "x", "y", "z");
    barBazDebugLogger.trace(TEST_MARKER, "Hello World!", TEST_EXCEPTION);

    Assert.assertEquals("", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testExactLogLevel() {
    fooBarInfoLogger.info("Hello exact world!");
    Assert.assertEquals("foo.bar INFO - Hello exact world!", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testHigherLogLevel_1() {
    fooBarInfoLogger.warn("Hello higher world!");
    Assert.assertEquals("foo.bar WARN - Hello higher world!", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testHigherLogLevel_2() {
    fooBarInfoLogger.error("Hello higher world!");
    Assert.assertEquals("foo.bar ERROR - Hello higher world!", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testFormattedMessage_1() {
    barBazDebugLogger.debug("Hello {}!", "World");
    Assert.assertEquals("bar.baz DEBUG - Hello World!", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testFormattedMessage_2() {
    barBazDebugLogger.debug("{} {}!", 23, "people");
    Assert.assertEquals("bar.baz DEBUG - 23 people!", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testFormattedMessage_3() {
    fooBarInfoLogger.warn("{} {} {} {} {} {}...", 4, 8, 15, 16, 23, 42);
    Assert.assertEquals("foo.bar WARN - 4 8 15 16 23 42...", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
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
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void testException_1() {
    fooBarInfoLogger.error("Exception while processing data!", TEST_EXCEPTION);
    Assert.assertEquals(
        "foo.bar ERROR - Exception while processing data!", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void testException_2() {
    fooBarInfoLogger.error("{} = {} and Exception", "a", "b", TEST_EXCEPTION);
    Assert.assertEquals("foo.bar ERROR - a = b and Exception", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_trace_1() {
    traceLogger.trace("message");
    Assert.assertEquals("traceLogger TRACE - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_trace_2() {
    traceLogger.trace("message {}", "o");
    Assert.assertEquals("traceLogger TRACE - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_trace_3() {
    traceLogger.trace("message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger TRACE - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_trace_4() {
    traceLogger.trace("message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger TRACE - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_trace_5() {
    traceLogger.trace("message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger TRACE - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_trace_6() {
    traceLogger.trace(TEST_MARKER, "message");
    Assert.assertEquals("traceLogger TRACE - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_trace_7() {
    traceLogger.trace(TEST_MARKER, "message {}", "o");
    Assert.assertEquals("traceLogger TRACE - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_trace_8() {
    traceLogger.trace(TEST_MARKER, "message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger TRACE - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_trace_9() {
    traceLogger.trace(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger TRACE - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_trace_10() {
    traceLogger.trace(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger TRACE - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_debug_1() {
    traceLogger.debug("message");
    Assert.assertEquals("traceLogger DEBUG - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_debug_2() {
    traceLogger.debug("message {}", "o");
    Assert.assertEquals("traceLogger DEBUG - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_debug_3() {
    traceLogger.debug("message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger DEBUG - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_debug_4() {
    traceLogger.debug("message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger DEBUG - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_debug_5() {
    traceLogger.debug("message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger DEBUG - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_debug_6() {
    traceLogger.debug(TEST_MARKER, "message");
    Assert.assertEquals("traceLogger DEBUG - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_debug_7() {
    traceLogger.debug(TEST_MARKER, "message {}", "o");
    Assert.assertEquals("traceLogger DEBUG - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_debug_8() {
    traceLogger.debug(TEST_MARKER, "message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger DEBUG - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_debug_9() {
    traceLogger.debug(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger DEBUG - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_debug_10() {
    traceLogger.debug(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger DEBUG - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_info_1() {
    traceLogger.info("message");
    Assert.assertEquals("traceLogger INFO - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_info_2() {
    traceLogger.info("message {}", "o");
    Assert.assertEquals("traceLogger INFO - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_info_3() {
    traceLogger.info("message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger INFO - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_info_4() {
    traceLogger.info("message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger INFO - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_info_5() {
    traceLogger.info("message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger INFO - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_info_6() {
    traceLogger.info(TEST_MARKER, "message");
    Assert.assertEquals("traceLogger INFO - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_info_7() {
    traceLogger.info(TEST_MARKER, "message {}", "o");
    Assert.assertEquals("traceLogger INFO - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_info_8() {
    traceLogger.info(TEST_MARKER, "message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger INFO - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_info_9() {
    traceLogger.info(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger INFO - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_info_10() {
    traceLogger.info(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger INFO - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_warn_1() {
    traceLogger.warn("message");
    Assert.assertEquals("traceLogger WARN - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_warn_2() {
    traceLogger.warn("message {}", "o");
    Assert.assertEquals("traceLogger WARN - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_warn_3() {
    traceLogger.warn("message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger WARN - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_warn_4() {
    traceLogger.warn("message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger WARN - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_warn_5() {
    traceLogger.warn("message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger WARN - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_warn_6() {
    traceLogger.warn(TEST_MARKER, "message");
    Assert.assertEquals("traceLogger WARN - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_warn_7() {
    traceLogger.warn(TEST_MARKER, "message {}", "o");
    Assert.assertEquals("traceLogger WARN - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_warn_8() {
    traceLogger.warn(TEST_MARKER, "message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger WARN - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_warn_9() {
    traceLogger.warn(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger WARN - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_warn_10() {
    traceLogger.warn(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger WARN - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_error_1() {
    traceLogger.error("message");
    Assert.assertEquals("traceLogger ERROR - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_error_2() {
    traceLogger.error("message {}", "o");
    Assert.assertEquals("traceLogger ERROR - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_error_3() {
    traceLogger.error("message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger ERROR - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_error_4() {
    traceLogger.error("message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger ERROR - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_error_5() {
    traceLogger.error("message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger ERROR - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_error_6() {
    traceLogger.error(TEST_MARKER, "message");
    Assert.assertEquals("traceLogger ERROR - message", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_error_7() {
    traceLogger.error(TEST_MARKER, "message {}", "o");
    Assert.assertEquals("traceLogger ERROR - message o", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_error_8() {
    traceLogger.error(TEST_MARKER, "message {} {}", "o", "o1");
    Assert.assertEquals("traceLogger ERROR - message o o1", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_error_9() {
    traceLogger.error(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    Assert.assertEquals("traceLogger ERROR - message o o1 o2 o3", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }

  @Test
  public void test_error_10() {
    traceLogger.error(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    Assert.assertEquals("traceLogger ERROR - message s", systemOutContent.toString());
    testForExceptionOutput(TEST_EXCEPTION);
  }

  private void testForExceptionOutput(Throwable t) {
    Scanner scanner = new Scanner(systemErrContent.toString());
    Assert.assertEquals(t.getClass().getName() + ": " + t.getMessage(), scanner.nextLine());
    while (scanner.hasNext()) {
      Assert.assertTrue(scanner.nextLine().matches("\tat .*?\\((.*?:\\d+|Native Method)\\)"));
    }
  }

  private static final Throwable TEST_EXCEPTION = new Exception("This is a test exception!");
  private static final Marker TEST_MARKER = MarkerFactory.getMarker("test marker");
}
