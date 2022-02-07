/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import picocli.CommandLine;

public class MainCommandTest extends StdOutAndStdErrCapturingTest {

  @Test
  public void testExitCode() {
    int exitCode = new CommandLine(new MainCommand()).execute();
    assertThat(exitCode, is(ExitCodes.MISSING_OR_INVALID_ARGUMENTS));
  }

  @Test
  public void testHelpOutput() {
    new CommandLine(new MainCommand()).execute();
    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), containsString("Usage: sf-fx-runtime-java"));
    assertThat(
        systemErrContent.toString(),
        containsString(
            "See 'sf-fx-runtime-java help <command>' to read about a specific subcommand."));
  }
}
