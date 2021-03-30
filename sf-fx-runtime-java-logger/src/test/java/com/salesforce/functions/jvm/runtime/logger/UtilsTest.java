/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

  @Test
  public void testShortenLoggerName() {
    String loggerName = "mainPackage.sub.sample.Bar";

    Map<Integer, String> expectations = new HashMap<>();
    expectations.put(0, "m.s.s.Bar");
    expectations.put(5, "m.s.s.Bar");
    expectations.put(10, "m.s.s.Bar");
    expectations.put(15, "m.s.sample.Bar");
    expectations.put(16, "m.sub.sample.Bar");
    expectations.put(26, "mainPackage.sub.sample.Bar");

    for (Map.Entry<Integer, String> entry : expectations.entrySet()) {
      Assert.assertEquals(entry.getValue(), Utils.shortenLoggerName(loggerName, entry.getKey()));
    }
  }

  @Test
  public void testShortenLoggerNameWithEmptySegment() {
    String loggerName = "foo.bar.baz..blah.Logger";
    Assert.assertEquals("f.b.b..b.Logger", Utils.shortenLoggerName(loggerName, 10));
  }
}
