/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

public class FunctionsLoggerFactoryTest {
  private static final FunctionsLoggerFactory FACTORY = new FunctionsLoggerFactory();

  @Test
  public void test() {
    Logger logger = FACTORY.getLogger("mylogger");
    Assert.assertEquals("mylogger", logger.getName());
    // Note: We do not test the log level of the logger since it's dependent on the environment
    // variables and would cause unstable tests. We do test the environment variable rules in a
    // separate test though: LoggingConfiguratorTest
  }
}
