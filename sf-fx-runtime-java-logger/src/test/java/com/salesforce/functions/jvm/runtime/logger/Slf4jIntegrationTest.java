/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jIntegrationTest extends StdOutAndStdErrCapturingTest {

  @Test
  public void testShortenedLoggerName() {
    Logger logger = LoggerFactory.getLogger("foo");
    logger.info("Hello World!");

    assertThat(systemErrContent.toString(), is(emptyString()));
    assertThat(systemOutContent.toString(), is(not(emptyString())));
  }
}
