/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class LogFmtTest {

  @Test
  public void testFormatEscaping() {
    // Many of the tests below are taken from go-logfmt:
    // https://github.com/go-logfmt/logfmt/blob/4e64dd1ccc01f924a2c20cfe88de4817ef7b1359/encode_test.go#L21-L60
    List<Spec> specList = new ArrayList<>();
    specList.add(new Spec("k", "v", "k=v"));
    specList.add(new Spec("k", "🦊", "k=🦊"));
    specList.add(new Spec("\\", "v", "\\=v"));
    specList.add(new Spec("k", "", "k="));
    specList.add(new Spec("k", null, "k=\"null\""));
    specList.add(new Spec("k", "null", "k=\"null\""));
    specList.add(new Spec("k", "v v", "k=\"v v\""));
    specList.add(new Spec("k", " ", "k=\" \""));
    specList.add(new Spec("k", "\"", "k=\"\\\"\""));
    specList.add(new Spec("k", "=", "k=\"=\""));
    specList.add(new Spec("k", "\\", "k=\\"));
    specList.add(new Spec("k", "=\\", "k=\"=\\\\\""));
    specList.add(new Spec("k", "\"", "k=\"\\\"\""));
    specList.add(new Spec("k", "\n", "k=\"\\n\""));
    specList.add(new Spec("k", "\r", "k=\"\\r\""));
    specList.add(new Spec("k", "\t", "k=\"\\t\""));
    specList.add(new Spec("k", "\u0010", "k=\"\\u0010\""));

    for (Spec spec : specList) {
      assertThat(LogFmt.format(spec.getAsMap()), is(equalTo(spec.getExpectedResult())));
    }
  }

  @Test
  public void testMultiple() {
    Map<String, String> keyValuePairs = new HashMap<>();
    keyValuePairs.put("foo", "bar");
    keyValuePairs.put("ham", "eggs");

    assertThat(LogFmt.format(keyValuePairs), is(equalTo("ham=eggs foo=bar")));
  }

  private static class Spec {
    private final String key;
    private final String value;
    private final String expectedResult;

    public Spec(String key, String value, String expectedResult) {
      this.key = key;
      this.value = value;
      this.expectedResult = expectedResult;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public String getExpectedResult() {
      return expectedResult;
    }

    public Map<String, String> getAsMap() {
      Map<String, String> returnValue = new HashMap<>();
      returnValue.put(getKey(), getValue());
      return returnValue;
    }
  }
}
