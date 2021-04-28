/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Scanner;
import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

public class FunctionsLoggerTest extends StdOutAndStdErrCapturingTest {
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

  @Test
  public void setGetName() {
    assertThat(fooBarInfoLogger.getName(), is(equalTo("foo.bar")));
    assertThat(barBazDebugLogger.getName(), is(equalTo("bar.baz")));
    assertThat(traceLogger.getName(), is(equalTo("traceLogger")));
  }

  @Test
  public void testIsTraceEnabled() {
    assertThat(traceLogger.isTraceEnabled(), is(true));
    assertThat(fooBarInfoLogger.isTraceEnabled(), is(false));
    assertThat(barBazDebugLogger.isTraceEnabled(), is(false));

    assertThat(traceLogger.isTraceEnabled(TEST_MARKER), is(true));
    assertThat(fooBarInfoLogger.isTraceEnabled(TEST_MARKER), is(false));
    assertThat(barBazDebugLogger.isTraceEnabled(TEST_MARKER), is(false));
  }

  @Test
  public void testIsDebugEnabled() {
    assertThat(traceLogger.isDebugEnabled(), is(true));
    assertThat(fooBarInfoLogger.isDebugEnabled(), is(false));
    assertThat(barBazDebugLogger.isDebugEnabled(), is(true));

    assertThat(traceLogger.isDebugEnabled(TEST_MARKER), is(true));
    assertThat(fooBarInfoLogger.isDebugEnabled(TEST_MARKER), is(false));
    assertThat(barBazDebugLogger.isDebugEnabled(TEST_MARKER), is(true));
  }

  @Test
  public void testIsInfoEnabled() {
    assertThat(traceLogger.isInfoEnabled(), is(true));
    assertThat(fooBarInfoLogger.isInfoEnabled(), is(true));
    assertThat(barBazDebugLogger.isInfoEnabled(), is(true));

    assertThat(traceLogger.isInfoEnabled(TEST_MARKER), is(true));
    assertThat(fooBarInfoLogger.isInfoEnabled(TEST_MARKER), is(true));
    assertThat(barBazDebugLogger.isInfoEnabled(TEST_MARKER), is(true));
  }

  @Test
  public void testIsWarnEnabled() {
    assertThat(traceLogger.isWarnEnabled(), is(true));
    assertThat(fooBarInfoLogger.isWarnEnabled(), is(true));
    assertThat(barBazDebugLogger.isWarnEnabled(), is(true));

    assertThat(traceLogger.isWarnEnabled(TEST_MARKER), is(true));
    assertThat(fooBarInfoLogger.isWarnEnabled(TEST_MARKER), is(true));
    assertThat(barBazDebugLogger.isWarnEnabled(TEST_MARKER), is(true));
  }

  @Test
  public void testIsErrorEnabled() {
    assertThat(traceLogger.isErrorEnabled(), is(true));
    assertThat(fooBarInfoLogger.isErrorEnabled(), is(true));
    assertThat(barBazDebugLogger.isErrorEnabled(), is(true));

    assertThat(traceLogger.isErrorEnabled(TEST_MARKER), is(true));
    assertThat(fooBarInfoLogger.isErrorEnabled(TEST_MARKER), is(true));
    assertThat(barBazDebugLogger.isErrorEnabled(TEST_MARKER), is(true));
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

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
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

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
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

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testExactLogLevel() {
    fooBarInfoLogger.info("Hello exact world!");

    assertThat(systemOutContent.toString(), is(equalTo("foo.bar INFO - Hello exact world!")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testHigherLogLevel_1() {
    fooBarInfoLogger.warn("Hello higher world!");

    assertThat(systemOutContent.toString(), is(equalTo("foo.bar WARN - Hello higher world!")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testHigherLogLevel_2() {
    fooBarInfoLogger.error("Hello higher world!");

    assertThat(systemOutContent.toString(), is(equalTo("foo.bar ERROR - Hello higher world!")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testFormattedMessage_1() {
    barBazDebugLogger.debug("Hello {}!", "World");
    assertThat(systemOutContent.toString(), is(equalTo("bar.baz DEBUG - Hello World!")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testFormattedMessage_2() {
    barBazDebugLogger.debug("{} {}!", 23, "people");
    assertThat(systemOutContent.toString(), is(equalTo("bar.baz DEBUG - 23 people!")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testFormattedMessage_3() {
    fooBarInfoLogger.warn("{} {} {} {} {} {}...", 4, 8, 15, 16, 23, 42);
    assertThat(systemOutContent.toString(), is(equalTo("foo.bar WARN - 4 8 15 16 23 42...")));
    assertThat(systemErrContent.toString(), is(emptyString()));
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
    assertThat(
        systemOutContent.toString(), is(equalTo("foo.bar ERROR - Result: Foo {internal=bar}")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testException_1() {
    fooBarInfoLogger.error("Exception while processing data!", TEST_EXCEPTION);

    assertThat(
        systemOutContent.toString(),
        is(equalTo("foo.bar ERROR - Exception while processing data!")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void testException_2() {
    fooBarInfoLogger.error("{} = {} and Exception", "a", "b", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("foo.bar ERROR - a = b and Exception")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_trace_1() {
    traceLogger.trace("message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_trace_2() {
    traceLogger.trace("message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_trace_3() {
    traceLogger.trace("message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_trace_4() {
    traceLogger.trace("message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_trace_5() {
    traceLogger.trace("message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_trace_6() {
    traceLogger.trace(TEST_MARKER, "message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_trace_7() {
    traceLogger.trace(TEST_MARKER, "message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_trace_8() {
    traceLogger.trace(TEST_MARKER, "message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_trace_9() {
    traceLogger.trace(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_trace_10() {
    traceLogger.trace(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger TRACE - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_debug_1() {
    traceLogger.debug("message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_debug_2() {
    traceLogger.debug("message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_debug_3() {
    traceLogger.debug("message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_debug_4() {
    traceLogger.debug("message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_debug_5() {
    traceLogger.debug("message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_debug_6() {
    traceLogger.debug(TEST_MARKER, "message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_debug_7() {
    traceLogger.debug(TEST_MARKER, "message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_debug_8() {
    traceLogger.debug(TEST_MARKER, "message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_debug_9() {
    traceLogger.debug(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_debug_10() {
    traceLogger.debug(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger DEBUG - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_info_1() {
    traceLogger.info("message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_info_2() {
    traceLogger.info("message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_info_3() {
    traceLogger.info("message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_info_4() {
    traceLogger.info("message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_info_5() {
    traceLogger.info("message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_info_6() {
    traceLogger.info(TEST_MARKER, "message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_info_7() {
    traceLogger.info(TEST_MARKER, "message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_info_8() {
    traceLogger.info(TEST_MARKER, "message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_info_9() {
    traceLogger.info(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_info_10() {
    traceLogger.info(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger INFO - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_warn_1() {
    traceLogger.warn("message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_warn_2() {
    traceLogger.warn("message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_warn_3() {
    traceLogger.warn("message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_warn_4() {
    traceLogger.warn("message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_warn_5() {
    traceLogger.warn("message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_warn_6() {
    traceLogger.warn(TEST_MARKER, "message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_warn_7() {
    traceLogger.warn(TEST_MARKER, "message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_warn_8() {
    traceLogger.warn(TEST_MARKER, "message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_warn_9() {
    traceLogger.warn(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_warn_10() {
    traceLogger.warn(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger WARN - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_error_1() {
    traceLogger.error("message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_error_2() {
    traceLogger.error("message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_error_3() {
    traceLogger.error("message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_error_4() {
    traceLogger.error("message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_error_5() {
    traceLogger.error("message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  @Test
  public void test_error_6() {
    traceLogger.error(TEST_MARKER, "message");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_error_7() {
    traceLogger.error(TEST_MARKER, "message {}", "o");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message o")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_error_8() {
    traceLogger.error(TEST_MARKER, "message {} {}", "o", "o1");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message o o1")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_error_9() {
    traceLogger.error(TEST_MARKER, "message {} {} {} {}", "o", "o1", "o2", "o3");
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message o o1 o2 o3")));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void test_error_10() {
    traceLogger.error(TEST_MARKER, "message {}", "s", TEST_EXCEPTION);
    assertThat(systemOutContent.toString(), is(equalTo("traceLogger ERROR - message s")));
    testForExceptionOutput(TEST_EXCEPTION);
  }

  private void testForExceptionOutput(Throwable t) {
    Scanner scanner = new Scanner(systemErrContent.toString());

    assertThat(scanner.nextLine(), is(equalTo(t.getClass().getName() + ": " + t.getMessage())));
    while (scanner.hasNext()) {
      assertThat(scanner.nextLine(), matchesRegex("\tat .*?\\((.*?:\\d+|Native Method)\\)"));
    }
  }

  private static final Throwable TEST_EXCEPTION = new Exception("This is a test exception!");
  private static final Marker TEST_MARKER = MarkerFactory.getMarker("test marker");
}
