/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import com.salesforce.functions.jvm.runtime.invocation.undertow.UndertowInvocationInterface;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.builder.bundle.FunctionBundleProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.builder.maven.MavenProjectBuilder;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "serve", header = "Serves a function project via HTTP")
public class ServeCommand implements Callable<Integer> {
  @Parameters(index = "0", description = "The directory that contains the function(s)")
  private Path projectPath;

  @Option(
      names = {"-p", "--port"},
      description = "The port the webserver should listen on. Defaults to '8080'.",
      defaultValue = "8080")
  private int port;

  @Option(
      names = {"-h", "--host"},
      description = "The host the webserver should bind to. Defaults to 'localhost'.",
      defaultValue = "localhost")
  private String host;

  private final List<ProjectBuilder> projectBuilders =
      Arrays.asList(new FunctionBundleProjectBuilder(), new MavenProjectBuilder());

  @Override
  public Integer call() throws Exception {
    return new ServeCommandImpl(
            projectPath, projectBuilders, new UndertowInvocationInterface(port, host))
        .call();
  }
}
