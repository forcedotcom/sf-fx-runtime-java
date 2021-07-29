/*
 * Copyright (c) 2021, salesforce.com, inc.
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
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.text.translate.LookupTranslator;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public class DefaultLoggingFormatter implements LoggingFormatter {
  private final Clock clock;
  private final LookupTranslator lookupTranslator;

  public DefaultLoggingFormatter(Clock clock) {
    this.clock = clock;

    Map<CharSequence, CharSequence> mapping = new HashMap<>();
    mapping.put("\"", "\\\"");
    mapping.put("\\", "\\\\");
    mapping.put("=", "\\=");

    this.lookupTranslator = new LookupTranslator(mapping);
  }

  @Override
  public String format(String loggerName, Level level, String message) {
    String invocationId = MDC.get("function-invocation-id");
    String localDateTimeString =
        ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    Map<String, String> stringFields =
        new HashMap<String, String>() {
          {
            put("localDateTime", localDateTimeString);
            put("level", level.toString());
            put("invocationId", invocationId);
            put("loggerName", Utils.shortenLoggerName(loggerName, 36));
            put("message", message);
          }
        };

    return formatString(stringFields);
  }

  private String formatString(Map<String, String> stringFields) {
    return stringFields.entrySet().stream()
        .map(
            entry ->
                String.format(
                    "%s=%s",
                    logFmtQuoteEscape(entry.getKey()), logFmtQuoteEscape(entry.getValue())))
        .collect(Collectors.joining(" "));
  }

  private String logFmtQuoteEscape(String string) {
    string = lookupTranslator.translate(Objects.toString(string, "null"));

    if (string.contains(" ")) {
      return "\"" + string + "\"";
    }

    return string;
  }
}
