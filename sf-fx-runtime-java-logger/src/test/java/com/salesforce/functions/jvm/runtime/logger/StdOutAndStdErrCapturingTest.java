/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;

public abstract class StdOutAndStdErrCapturingTest {
  protected final PrintStream previousSystemOut = System.out;
  protected final PrintStream previousSystemErr = System.err;
  protected final ByteArrayOutputStream systemOutContent = new ByteArrayOutputStream();
  protected final ByteArrayOutputStream systemErrContent = new ByteArrayOutputStream();

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
}
