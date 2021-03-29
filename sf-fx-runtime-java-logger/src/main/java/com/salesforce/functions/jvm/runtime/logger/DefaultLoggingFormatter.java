/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public class DefaultLoggingFormatter implements LoggingFormatter {
  private final Clock clock;

  public DefaultLoggingFormatter(Clock clock) {
    this.clock = clock;
  }

  @Override
  public String format(String loggerName, Level level, String message) {
    String invocationId = MDC.get("function-invocation-id");
    String localDateTimeString = LocalDateTime.now(clock).format(DATE_TIME_FORMATTER);

    return String.format(
        FORMAT_STRING,
        localDateTimeString,
        level.toString(),
        invocationId,
        Utils.shortenLoggerName(loggerName, 36),
        message);
  }

  private static final String FORMAT_STRING = "%s %-5s [INVOCATION %s] %s - %s\n";

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
}
