/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import com.salesforce.functions.jvm.runtime.bundle.FunctionBundler;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.builder.maven.MavenProjectBuilder;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionsProjectFunctionsScanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "bundle", header = "Pre-bundles a function project")
public class BundleCommand implements Callable<Integer> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BundleCommand.class);

  @CommandLine.Parameters(index = "0", description = "The directory that contains the function(s)")
  private Path projectPath;

  @CommandLine.Parameters(index = "1", description = "The directory to write the bundle to")
  private Path bundlePath;

  private final List<ProjectBuilder> projectBuilders = Arrays.asList(new MavenProjectBuilder());

  @Override
  public Integer call() throws Exception {
    if (!Files.exists(bundlePath)) {
      Files.createDirectories(bundlePath);
    } else if (!Files.isDirectory(bundlePath)) {
      System.err.println("Bundle path '" + bundlePath + "' must be an empty directory!");
      return EXIT_CODE_BUNDLE_DIRECTORY_NOT_A_DIRECTORY;
    } else if (Files.list(bundlePath).count() > 0) {
      System.err.println("Bundle path '" + bundlePath + "' must be empty!");
      return EXIT_CODE_BUNDLE_DIRECTORY_NOT_EMPTY;
    }

    LOGGER.info("Detecting project type at path {}...", projectPath);
    long projectDetectStart = System.currentTimeMillis();

    Optional<Project> optionalProject = Optional.empty();
    for (ProjectBuilder builder : projectBuilders) {
      optionalProject = builder.build(projectPath);

      if (optionalProject.isPresent()) {
        break;
      }
    }

    Project project =
        optionalProject.orElseThrow(
            () ->
                new IllegalStateException(
                    String.format("Could not find project at path %s!", projectPath)));

    long projectDetectDuration = System.currentTimeMillis() - projectDetectStart;
    LOGGER.info(
        "Detected {} project at path {} after {}ms!",
        project.getTypeName(),
        projectPath,
        projectDetectDuration);

    LOGGER.info("Scanning project for functions...");
    long scanStart = System.currentTimeMillis();
    List<SalesforceFunction> functions =
        new SalesforceFunctionsProjectFunctionsScanner().scan(project);
    long scanDuration = System.currentTimeMillis() - scanStart;
    LOGGER.info("Found {} function(s) after {}ms.", functions.size(), scanDuration);

    if (functions.isEmpty()) {
      return EXIT_CODE_NO_FUNCTIONS_FOUND;
    }

    if (functions.size() > 1) {
      return EXIT_CODE_MULTIPLE_FUNCTIONS_FOUND;
    }

    SalesforceFunction function = functions.get(0);

    FunctionBundler.bundle(project, function, bundlePath);

    return EXIT_CODE_SUCCESS;
  }

  private final int EXIT_CODE_SUCCESS = 0;
  private final int EXIT_CODE_NO_FUNCTIONS_FOUND = 1;
  private final int EXIT_CODE_MULTIPLE_FUNCTIONS_FOUND = 2;
  private final int EXIT_CODE_CANNOT_WRITE_BUNDLE = 3;
  private final int EXIT_CODE_BUNDLE_DIRECTORY_NOT_EMPTY = 4;
  private final int EXIT_CODE_BUNDLE_DIRECTORY_NOT_A_DIRECTORY = 5;
  private final int EXIT_CODE_UNEXPECTED_FILE_TYPE = 6;
}
