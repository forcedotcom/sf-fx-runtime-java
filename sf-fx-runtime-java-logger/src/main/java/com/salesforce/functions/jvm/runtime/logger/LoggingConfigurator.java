/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import static com.salesforce.functions.jvm.runtime.logger.Constants.LOGGER_NAME_SEGMENT_DELIMITER;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.event.Level;

public class LoggingConfigurator {

  public static LoggingConfiguration configureFromEnvironment(Map<String, String> environment) {
    Map<String, Level> logLevels = new HashMap<>();
    Level rootLevel = Level.INFO;

    for (Map.Entry<String, String> environmentVariable : environment.entrySet()) {
      String envVarKey = environmentVariable.getKey();
      String envVarValue = environmentVariable.getValue();

      if (envVarKey.equals(ROOT_LOGLEVEL_ENV_VAR_NAME)) {
        Optional<Level> newRootLogLevel = levelFromString(envVarValue);
        if (newRootLogLevel.isPresent()) {
          rootLevel = newRootLogLevel.get();
        } else {
          printlnInvalidEnvironmentVariableWarning(envVarKey, envVarValue);
        }

        continue;
      }

      if (envVarKey.startsWith(LOGLEVEL_ENV_VAR_PREFIX)) {
        String loggerName = envVarKey.substring(LOGLEVEL_ENV_VAR_PREFIX.length());
        Optional<Level> loggerLevel = levelFromString(envVarValue);

        if (loggerLevel.isPresent()) {
          // Environment variables cannot contain dots. To delimit logger names in environment
          // variables, underscore is used instead. The rest of this logger implementation uses dots
          // as the delimiter, so we normalize them here before proceeding.
          String normalizedLoggerName =
              loggerName.replaceAll(
                  Pattern.quote(ENV_VAR_LOGGER_NAME_SEGMENT_DELIMITER),
                  LOGGER_NAME_SEGMENT_DELIMITER);

          logLevels.put(normalizedLoggerName, loggerLevel.get());
        } else {
          printlnInvalidEnvironmentVariableWarning(envVarKey, envVarValue);
        }
      }
    }

    return new LoggingConfiguration(rootLevel, logLevels);
  }

  private static Optional<Level> levelFromString(String logLevelString) {
    switch (logLevelString.toLowerCase()) {
      case "debug":
        return Optional.of(Level.DEBUG);
      case "info":
        return Optional.of(Level.INFO);
      case "warn":
        return Optional.of(Level.WARN);
      case "trace":
        return Optional.of(Level.TRACE);
      case "error":
        return Optional.of(Level.ERROR);
      default:
        return Optional.empty();
    }
  }

  private static void printlnInvalidEnvironmentVariableWarning(
      String envVarKey, String envVarValue) {
    System.err.printf(
        "WARNING: Environment variable '%s' contains unknown log level '%s'.\n",
        envVarKey, envVarValue);
    System.err.printf("WARNING: Environment variable '%s' will be ignored!\n", envVarKey);
  }

  private static final String ROOT_LOGLEVEL_ENV_VAR_NAME = "SF_FN_LOGLEVEL";
  private static final String LOGLEVEL_ENV_VAR_PREFIX = ROOT_LOGLEVEL_ENV_VAR_NAME + "_";
  private static final String ENV_VAR_LOGGER_NAME_SEGMENT_DELIMITER = "_";
}
