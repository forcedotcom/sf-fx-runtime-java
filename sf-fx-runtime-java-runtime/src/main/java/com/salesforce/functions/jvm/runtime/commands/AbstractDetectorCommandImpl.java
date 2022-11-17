/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import static com.salesforce.functions.jvm.runtime.Constants.DEFAULT_SALESFORCE_API_VERSION;

import com.google.common.base.Splitter;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.ProjectMetadata;
import com.salesforce.functions.jvm.runtime.project.ProjectMetadataParser;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionsProjectFunctionsScanner;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDetectorCommandImpl implements Callable<Integer> {
  protected final Path projectPath;
  protected final List<ProjectBuilder> projectBuilders;

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDetectorCommandImpl.class);

  protected AbstractDetectorCommandImpl(Path projectPath, List<ProjectBuilder> projectBuilders) {
    this.projectPath = projectPath;
    this.projectBuilders = projectBuilders;
  }

  @Override
  public final Integer call() throws Exception {
    LOGGER.info("Detecting project type at path {}...", projectPath);
    long projectDetectStart = System.currentTimeMillis();

    Optional<Project> optionalProject = Optional.empty();
    for (ProjectBuilder builder : projectBuilders) {
      optionalProject = builder.build(projectPath);

      if (optionalProject.isPresent()) {
        break;
      }
    }

    if (!optionalProject.isPresent()) {
      LOGGER.info("Could not find project at path {}!", projectPath);
      return ExitCodes.NO_PROJECT_FOUND;
    }

    Project project = optionalProject.get();

    long projectDetectDuration = System.currentTimeMillis() - projectDetectStart;
    LOGGER.info(
        "Detected {} project at path {} after {}ms!",
        project.getTypeName(),
        projectPath,
        projectDetectDuration);

    LOGGER.info("Reading project metadata at path {}...", projectPath);
    ProjectMetadata projectMetadata =
        ProjectMetadataParser.parse(projectPath.resolve("project.toml"))
            .orElseThrow(
                () -> new IllegalStateException("Cannot parse project metadata (project.toml)!"));

    String salesforceApiVersion = DEFAULT_SALESFORCE_API_VERSION;

    if (projectMetadata.getSalesforceApiVersion().isPresent()) {
      salesforceApiVersion = projectMetadata.getSalesforceApiVersion().get();
    } else {
      LOGGER.warn(
          "Project's Salesforce API version isn't explicitly defined in project.toml. The default version {} will be used.",
          DEFAULT_SALESFORCE_API_VERSION);
    }

    if (isVersionSupported(salesforceApiVersion)) {
      LOGGER.info("Project uses Salesforce API version {}.", salesforceApiVersion);
    } else {
      LOGGER.error(
          "Salesforce Rest API Version \"{}\" is not supported. Please change `com.salesforce.salesforce-api-version` in project.toml to \"{}\" or newer.",
          salesforceApiVersion,
          DEFAULT_SALESFORCE_API_VERSION);
      return ExitCodes.UNSUPPORTED_SALESFORCE_API_VERSION;
    }

    LOGGER.info("Scanning project for functions...");
    long scanStart = System.currentTimeMillis();
    List<SalesforceFunction> functions =
        new SalesforceFunctionsProjectFunctionsScanner(salesforceApiVersion).scan(project);
    long scanDuration = System.currentTimeMillis() - scanStart;
    LOGGER.info("Found {} function(s) after {}ms.", functions.size(), scanDuration);

    return handle(project, functions);
  }

  private static boolean isVersionSupported(String salesforceApiVersion) {
    return parseMajor(salesforceApiVersion)
        .flatMap(
            targetVersion ->
                parseMajor(DEFAULT_SALESFORCE_API_VERSION)
                    .map(minimumVersion -> targetVersion >= minimumVersion))
        .orElse(false);
  }

  private static Optional<Integer> parseMajor(String version) {
    try {
      List<String> semverParts = Splitter.on('.').splitToList(version);
      return Optional.of(Integer.parseInt(semverParts.get(0), 10));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  protected abstract Integer handle(Project project, List<SalesforceFunction> functions)
      throws Exception;
}
