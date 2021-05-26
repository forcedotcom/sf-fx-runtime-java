/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import static com.salesforce.functions.jvm.runtime.commands.ExitCodes.*;

import com.salesforce.functions.jvm.runtime.bundle.FunctionBundler;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BundleCommandImpl extends AbstractDetectorCommandImpl {
  private final Path bundlePath;
  private static final Logger LOGGER = LoggerFactory.getLogger(BundleCommandImpl.class);

  public BundleCommandImpl(
      Path projectPath, Path bundlePath, List<ProjectBuilder> projectBuilders) {

    super(projectPath, projectBuilders);
    this.bundlePath = bundlePath;
  }

  @Override
  protected Integer handle(Project project, List<SalesforceFunction> functions) throws Exception {
    if (!Files.exists(bundlePath)) {
      Files.createDirectories(bundlePath);
    } else if (!Files.isDirectory(bundlePath)) {
      LOGGER.error("Bundle path {} must be an empty directory!", bundlePath);
      return BUNDLE_DIRECTORY_NOT_A_DIRECTORY;
    } else if (Files.list(bundlePath).count() > 0) {
      LOGGER.error("Bundle path {} must be empty!", bundlePath);
      return BUNDLE_DIRECTORY_NOT_EMPTY;
    }

    if (functions.isEmpty()) {
      return NO_FUNCTIONS_FOUND;
    }

    if (functions.size() > 1) {
      return MULTIPLE_FUNCTIONS_FOUND;
    }

    FunctionBundler.bundle(project, functions.get(0), bundlePath);

    return SUCCESS;
  }
}
