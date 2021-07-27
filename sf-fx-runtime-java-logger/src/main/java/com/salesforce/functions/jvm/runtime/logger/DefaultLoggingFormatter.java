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
import java.util.HashMap;
import java.util.Map;
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
    String zoneId = clock.getZone().toString();
    Map<String, String> stringFields =
        new HashMap<String, String>() {
          {
            put(zoneId, localDateTimeString);
            put("level", level.toString());
            put("invocationId", invocationId);
            put("loggerName", Utils.shortenLoggerName(loggerName, 36));
            put("message", message);
          }
        };

    String formattedString = formatString(stringFields);

    return formattedString;
  }

  private String formatString(Map<String, String> stringFields) {
    StringBuilder formattedString = new StringBuilder();
    for (String field : stringFields.keySet()) {
      String currentFieldValue = String.format("\"%s\"=\"%s\" ", field, stringFields.get(field));
      formattedString.append(currentFieldValue);
    }
    formattedString.append("\n");
    return formattedString.toString();
  }

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
}
