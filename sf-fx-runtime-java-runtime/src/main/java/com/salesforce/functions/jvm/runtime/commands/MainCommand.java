/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
    name = "sf-fx-runtime-java",
    description = "Salesforce Functions Java Runtime",
    footer = "%nSee 'sf-fx-runtime-java help <command>' to read about a specific subcommand.",
    subcommands = {ServeCommand.class, BundleCommand.class, CommandLine.HelpCommand.class})
public class MainCommand implements Callable<Integer> {
  @Spec CommandSpec spec;

  @Override
  public Integer call() {
    spec.commandLine().usage(System.err);
    return ExitCodes.MISSING_OR_INVALID_ARGUMENTS;
  }
}
