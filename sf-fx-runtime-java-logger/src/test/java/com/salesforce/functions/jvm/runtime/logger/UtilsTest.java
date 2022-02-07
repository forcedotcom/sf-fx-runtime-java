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

import org.junit.Test;

public class UtilsTest {

  @Test
  public void testShortenLoggerName() {
    String loggerName = "mainPackage.sub.sample.Bar";

    assertThat(Utils.shortenLoggerName(loggerName, 0), is(equalTo("m.s.s.Bar")));
    assertThat(Utils.shortenLoggerName(loggerName, 5), is(equalTo("m.s.s.Bar")));
    assertThat(Utils.shortenLoggerName(loggerName, 10), is(equalTo("m.s.s.Bar")));
    assertThat(Utils.shortenLoggerName(loggerName, 15), is(equalTo("m.s.sample.Bar")));
    assertThat(Utils.shortenLoggerName(loggerName, 16), is(equalTo("m.sub.sample.Bar")));
    assertThat(Utils.shortenLoggerName(loggerName, 26), is(equalTo("mainPackage.sub.sample.Bar")));
  }

  @Test
  public void testShortenLoggerNameWithEmptySegment() {
    String loggerName = "foo.bar.baz..blah.Logger";
    assertThat(Utils.shortenLoggerName(loggerName, 10), is(equalTo("f.b.b..b.Logger")));
  }
}
