/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public class FunctionsLogger implements Logger {
  private final String name;
  private final Level loggerLevel;
  private final LoggingFormatter formatter;

  public FunctionsLogger(String name, Level loggerLevel, LoggingFormatter formatter) {
    this.name = name;
    this.loggerLevel = loggerLevel;
    this.formatter = formatter;
  }

  private void log(Level level, String message) {
    if (isLevelEnabled(level)) {
      log(level, message, (Throwable) null);
    }
  }

  private void log(Level level, String s, Object o) {
    if (isLevelEnabled(level)) {
      FormattingTuple formattingTuple = MessageFormatter.format(s, o);
      log(level, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }
  }

  private void log(Level level, String s, Object o, Object o1) {
    if (isLevelEnabled(level)) {
      FormattingTuple formattingTuple = MessageFormatter.format(s, o, o1);
      log(level, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }
  }

  private void log(Level level, String s, Object... objects) {
    if (isLevelEnabled(level)) {
      FormattingTuple formattingTuple = MessageFormatter.arrayFormat(s, objects);
      log(level, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }
  }

  private void log(Level level, String s, Throwable throwable) {
    if (isLevelEnabled(level)) {
      System.out.println(formatter.format(name, level, s));
      if (throwable != null) {
        throwable.printStackTrace();
      }
    }
  }

  private boolean isLevelEnabled(Level level) {
    return level.toInt() >= loggerLevel.toInt();
  }

  @Override
  public boolean isTraceEnabled() {
    return isLevelEnabled(Level.TRACE);
  }

  @Override
  public boolean isDebugEnabled() {
    return isLevelEnabled(Level.DEBUG);
  }

  @Override
  public boolean isInfoEnabled() {
    return isLevelEnabled(Level.INFO);
  }

  @Override
  public boolean isWarnEnabled() {
    return isLevelEnabled(Level.WARN);
  }

  @Override
  public boolean isErrorEnabled() {
    return isLevelEnabled(Level.ERROR);
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return isTraceEnabled();
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return isDebugEnabled();
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return isInfoEnabled();
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return isWarnEnabled();
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return isErrorEnabled();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void trace(String s) {
    log(Level.TRACE, s);
  }

  @Override
  public void trace(String s, Object o) {
    log(Level.TRACE, s, o);
  }

  @Override
  public void trace(String s, Object o, Object o1) {
    log(Level.TRACE, s, o, o1);
  }

  @Override
  public void trace(String s, Object... objects) {
    log(Level.TRACE, s, objects);
  }

  @Override
  public void trace(String s, Throwable throwable) {
    log(Level.TRACE, s, throwable);
  }

  @Override
  public void trace(Marker marker, String s) {
    trace(s);
  }

  @Override
  public void trace(Marker marker, String s, Object o) {
    trace(s, o);
  }

  @Override
  public void trace(Marker marker, String s, Object o, Object o1) {
    trace(s, o, o1);
  }

  @Override
  public void trace(Marker marker, String s, Object... objects) {
    trace(s, objects);
  }

  @Override
  public void trace(Marker marker, String s, Throwable throwable) {
    trace(s, throwable);
  }

  @Override
  public void debug(String s) {
    log(Level.DEBUG, s);
  }

  @Override
  public void debug(String s, Object o) {
    log(Level.DEBUG, s, o);
  }

  @Override
  public void debug(String s, Object o, Object o1) {
    log(Level.DEBUG, s, o, o1);
  }

  @Override
  public void debug(String s, Object... objects) {
    log(Level.DEBUG, s, objects);
  }

  @Override
  public void debug(String s, Throwable throwable) {
    log(Level.DEBUG, s, throwable);
  }

  @Override
  public void debug(Marker marker, String s) {
    debug(s);
  }

  @Override
  public void debug(Marker marker, String s, Object o) {
    debug(s, o);
  }

  @Override
  public void debug(Marker marker, String s, Object o, Object o1) {
    debug(s, o, o1);
  }

  @Override
  public void debug(Marker marker, String s, Object... objects) {
    debug(s, objects);
  }

  @Override
  public void debug(Marker marker, String s, Throwable throwable) {
    debug(s, throwable);
  }

  @Override
  public void info(String s) {
    log(Level.INFO, s);
  }

  @Override
  public void info(String s, Object o) {
    log(Level.INFO, s, o);
  }

  @Override
  public void info(String s, Object o, Object o1) {
    log(Level.INFO, s, o, o1);
  }

  @Override
  public void info(String s, Object... objects) {
    log(Level.INFO, s, objects);
  }

  @Override
  public void info(String s, Throwable throwable) {
    log(Level.INFO, s, throwable);
  }

  @Override
  public void info(Marker marker, String s) {
    info(s);
  }

  @Override
  public void info(Marker marker, String s, Object o) {
    info(s, o);
  }

  @Override
  public void info(Marker marker, String s, Object o, Object o1) {
    info(s, o, o1);
  }

  @Override
  public void info(Marker marker, String s, Object... objects) {
    info(s, objects);
  }

  @Override
  public void info(Marker marker, String s, Throwable throwable) {
    info(s, throwable);
  }

  @Override
  public void warn(String s) {
    log(Level.WARN, s);
  }

  @Override
  public void warn(String s, Object o) {
    log(Level.WARN, s, o);
  }

  @Override
  public void warn(String s, Object o, Object o1) {
    log(Level.WARN, s, o, o1);
  }

  @Override
  public void warn(String s, Object... objects) {
    log(Level.WARN, s, objects);
  }

  @Override
  public void warn(String s, Throwable throwable) {
    log(Level.WARN, s, throwable);
  }

  @Override
  public void warn(Marker marker, String s) {
    warn(s);
  }

  @Override
  public void warn(Marker marker, String s, Object o) {
    warn(s, o);
  }

  @Override
  public void warn(Marker marker, String s, Object o, Object o1) {
    warn(s, o, o1);
  }

  @Override
  public void warn(Marker marker, String s, Object... objects) {
    warn(s, objects);
  }

  @Override
  public void warn(Marker marker, String s, Throwable throwable) {
    warn(s, throwable);
  }

  @Override
  public void error(String s) {
    log(Level.ERROR, s);
  }

  @Override
  public void error(String s, Object o) {
    log(Level.ERROR, s, o);
  }

  @Override
  public void error(String s, Object o, Object o1) {
    log(Level.ERROR, s, o, o1);
  }

  @Override
  public void error(String s, Object... objects) {
    log(Level.ERROR, s, objects);
  }

  @Override
  public void error(String s, Throwable throwable) {
    log(Level.ERROR, s, throwable);
  }

  @Override
  public void error(Marker marker, String s) {
    error(s);
  }

  @Override
  public void error(Marker marker, String s, Object o) {
    error(s, o);
  }

  @Override
  public void error(Marker marker, String s, Object o, Object o1) {
    error(s, o, o1);
  }

  @Override
  public void error(Marker marker, String s, Object... objects) {
    error(s, objects);
  }

  @Override
  public void error(Marker marker, String s, Throwable throwable) {
    error(s, throwable);
  }
}
