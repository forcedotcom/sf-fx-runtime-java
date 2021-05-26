/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import static com.salesforce.functions.jvm.runtime.commands.ExitCodes.*;
import static picocli.AutoComplete.EXIT_CODE_SUCCESS;

import com.salesforce.functions.jvm.runtime.bundle.FunctionBundler;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BundleCommandImpl implements Callable<Integer> {
  private final Path projectPath;
  private final Path bundlePath;
  private final List<ProjectBuilder> projectBuilders;
  private static final Logger LOGGER = LoggerFactory.getLogger(BundleCommandImpl.class);

  public BundleCommandImpl(
      Path projectPath, Path bundlePath, List<ProjectBuilder> projectBuilders) {
    this.projectPath = projectPath;
    this.bundlePath = bundlePath;
    this.projectBuilders = projectBuilders;
  }

  @Override
  public Integer call() throws Exception {
    if (!Files.exists(bundlePath)) {
      Files.createDirectories(bundlePath);
    } else if (!Files.isDirectory(bundlePath)) {
      LOGGER.error("Bundle path {} must be an empty directory!", bundlePath);
      return BUNDLE_DIRECTORY_NOT_A_DIRECTORY;
    } else if (Files.list(bundlePath).count() > 0) {
      LOGGER.error("Bundle path {} must be empty!", bundlePath);
      return BUNDLE_DIRECTORY_NOT_EMPTY;
    }

    DetectionResult result = Detector.detect(projectPath, projectBuilders);
    List<SalesforceFunction> functions = result.getFunctions();

    if (!result.getProject().isPresent()) {
      return NO_PROJECT_FOUND;
    }

    if (functions.isEmpty()) {
      return NO_FUNCTIONS_FOUND;
    }

    if (functions.size() > 1) {
      return MULTIPLE_FUNCTIONS_FOUND;
    }

    FunctionBundler.bundle(result.getProject().get(), functions.get(0), bundlePath);

    return EXIT_CODE_SUCCESS;
  }
}
