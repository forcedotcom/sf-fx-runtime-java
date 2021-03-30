/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jIntegrationTest {
  private final PrintStream previousSystemOut = System.out;
  private final PrintStream previousSystemErr = System.err;
  private final ByteArrayOutputStream systemOutContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream systemErrContent = new ByteArrayOutputStream();

  @Before
  public void redirectOutputStreams() {
    System.setOut(new PrintStream(systemOutContent));
    System.setErr(new PrintStream(systemErrContent));
  }

  @After
  public void restoreOutputStreams() {
    System.setOut(previousSystemOut);
    System.setErr(previousSystemErr);
  }

  @Test
  public void testShortenedLoggerName() {
    Logger logger = LoggerFactory.getLogger("foo");
    logger.info("Hello World!");
    Assert.assertNotEquals("", systemOutContent.toString());
    Assert.assertEquals("", systemErrContent.toString());
  }
}
