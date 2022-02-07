/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public class LogFmtLoggingFormatter implements LoggingFormatter {
  private final Clock clock;

  public LogFmtLoggingFormatter(Clock clock) {
    this.clock = clock;
  }

  @Override
  public String format(String loggerName, Level level, String message) {
    String invocationId = MDC.get("function-invocation-id");
    String dateTimeString = ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    Map<String, String> logFields = new HashMap<>();
    logFields.put("dateTime", dateTimeString);
    logFields.put("level", level.toString());
    logFields.put("invocationId", invocationId);
    logFields.put("loggerName", Utils.shortenLoggerName(loggerName, 36));
    logFields.put("message", message);

    return LogFmt.format(logFields);
  }
}
