/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import com.salesforce.functions.jvm.runtime.project.builder.maven.MavenProjectBuilder;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "bundle", header = "Pre-bundles a function project")
public class BundleCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "The directory that contains the function(s)")
  private Path projectPath;

  @CommandLine.Parameters(index = "1", description = "The directory to write the bundle to")
  private Path bundlePath;

  @Override
  public Integer call() throws Exception {
    return new BundleCommandImpl(
            projectPath, bundlePath, Collections.singletonList(new MavenProjectBuilder()))
        .call();
  }
}
