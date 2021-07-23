/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public class DefaultLoggingFormatterTest {
  private final LoggingFormatter formatter =
      new DefaultLoggingFormatter(Clock.fixed(Instant.EPOCH, ZoneId.of("UTC")));

  @Before
  public void clearAndSetMDC() {
    MDC.clear();
    MDC.put("function-invocation-id", "e3a4ae2b-fefb-4277-89d0-7068e7e39b99");
  }

  @Test
  public void test_1() {
    String result = formatter.format("foo.bar.baz", Level.DEBUG, "This is a message!");
    assertThat(
        result,
        is(
            equalTo(
                "localDateTime=00:00:00.000 level=DEBUG InvocationId=e3a4ae2b-fefb-4277-89d0-7068e7e39b99 loggerName=foo.bar.baz message=This is a message! ")));
  }

  @Test
  public void testShortenedLoggerName() {
    String result =
        formatter.format(
            "com.salesforce.functions.jvm.runtime.logger.ClassName",
            Level.WARN,
            "This is a message!");

    assertThat(
        result,
        is(
            equalTo(
                "localDateTime=00:00:00.000 level=WARN InvocationId=e3a4ae2b-fefb-4277-89d0-7068e7e39b99 loggerName=c.s.f.jvm.runtime.logger.ClassName message=This is a message! ")));
  }

  @Test
  public void testEmptyMDC() {
    MDC.clear();
    String result =
        formatter.format(
            "com.salesforce.functions.jvm.runtime.logger.EmptyMDC",
            Level.TRACE,
            "This is a message!");

    assertThat(
        result,
        is(
            equalTo(
                "localDateTime=00:00:00.000 level=TRACE InvocationId=null loggerName=c.s.f.jvm.runtime.logger.EmptyMDC message=This is a message! ")));
  }
}
