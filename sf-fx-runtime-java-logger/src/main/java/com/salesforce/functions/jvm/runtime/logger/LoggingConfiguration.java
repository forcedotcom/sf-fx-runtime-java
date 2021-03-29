/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import static com.salesforce.functions.jvm.runtime.logger.Constants.LOGGER_NAME_SEGMENT_DELIMITER;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.event.Level;

public class LoggingConfiguration {
  private final Level rootLevel;
  private final Map<String, Level> logLevelByLoggerNames;

  public LoggingConfiguration(Level rootLevel, Map<String, Level> logLevelByLoggerNames) {
    this.rootLevel = rootLevel;
    this.logLevelByLoggerNames = Collections.unmodifiableMap(new HashMap<>(logLevelByLoggerNames));
  }

  public Level getRootLogLevel() {
    return rootLevel;
  }

  public Level getLogLevelForLoggerName(String loggerName) {
    String[] segments = loggerName.split(Pattern.quote(LOGGER_NAME_SEGMENT_DELIMITER));

    for (int i = loggerName.length() - 1; i > 0; i--) {
      String subPath =
          String.join(LOGGER_NAME_SEGMENT_DELIMITER, Arrays.copyOfRange(segments, 0, i));

      Level logLevel = logLevelByLoggerNames.get(subPath);
      if (logLevel != null) {
        return logLevel;
      }
    }

    return rootLevel;
  }
}
