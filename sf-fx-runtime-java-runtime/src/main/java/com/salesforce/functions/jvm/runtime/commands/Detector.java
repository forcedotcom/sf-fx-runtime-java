/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilderException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionsProjectFunctionsScanner;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Detector {
  private static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

  public static DetectionResult detect(Path projectPath, List<ProjectBuilder> projectBuilders)
      throws ProjectBuilderException {

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
      return new DetectionResult(null, Collections.emptyList());
    }

    Project project = optionalProject.get();

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

    return new DetectionResult(project, functions);
  }

  private Detector() {}
}
