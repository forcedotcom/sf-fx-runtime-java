/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import com.google.common.base.CharMatcher;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;
import org.apache.commons.text.translate.UnicodeEscaper;

public final class LogFmt {
  private static final CharSequenceTranslator VALUE_CHAR_SEQUENCE_TRANSLATOR;

  private static final CharMatcher VALUE_NEEDS_QUOTING_CHAR_MATCHER =
      CharMatcher.inRange((char) 0x00, ' ').or(CharMatcher.anyOf("=\" "));

  private static final CharMatcher ILLEGAL_KEY_CHAR_MATCHER =
      CharMatcher.inRange((char) 0x00, ' ').or(CharMatcher.anyOf("=\""));

  static {
    Map<CharSequence, CharSequence> mapping = new HashMap<>();
    mapping.put("\"", "\\\"");
    mapping.put("\\", "\\\\");
    mapping.put("\n", "\\n");
    mapping.put("\r", "\\r");
    mapping.put("\t", "\\t");

    VALUE_CHAR_SEQUENCE_TRANSLATOR = new LookupTranslator(mapping).with(UnicodeEscaper.below(' '));
  }

  /**
   * Formats the given key-value pairs into a logfmt line.
   *
   * <p>This implementation mimics what go-logfmt (https://github.com/go-logfmt/logfmt) does in
   * terms of escaping and quoting since there is no official spec this could be based on.
   *
   * @param keyValuePairs The key-value pairs to format.
   * @return The resulting logfmt line.
   */
  public static String format(Map<String, String> keyValuePairs) {
    return keyValuePairs.entrySet().stream()
        .map(
            entry ->
                String.format(
                    "%s=%s", sanitizeKey(entry.getKey()), sanitizeValue(entry.getValue())))
        .collect(Collectors.joining(" "));
  }

  private static String sanitizeKey(String key) {
    return ILLEGAL_KEY_CHAR_MATCHER.removeFrom(key);
  }

  private static String sanitizeValue(String value) {
    if (value == null
        || value.equals("null")
        || VALUE_NEEDS_QUOTING_CHAR_MATCHER.countIn(value) > 0) {

      return "\""
          + VALUE_CHAR_SEQUENCE_TRANSLATOR.translate(Objects.toString(value, "null"))
          + "\"";
    }

    return value;
  }

  private LogFmt() {}
}
