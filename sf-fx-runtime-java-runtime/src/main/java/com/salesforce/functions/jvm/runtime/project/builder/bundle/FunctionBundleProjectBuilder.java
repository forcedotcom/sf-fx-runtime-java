/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.bundle;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilderException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FunctionBundleProjectBuilder implements ProjectBuilder {
  @Override
  public Optional<Project> build(Path projectPath) throws ProjectBuilderException {
    Path classpathDirectoryPath = Paths.get(projectPath.toString(), "classpath");
    Path functionBundleTomlPath = Paths.get(projectPath.toString(), "function-bundle.toml");

    if (Files.isDirectory(classpathDirectoryPath) && Files.isRegularFile(functionBundleTomlPath)) {
      try {
        List<Path> classpathDirectoryEntries =
            Files.list(classpathDirectoryPath).collect(Collectors.toList());
        return Optional.of(new FunctionBundleProject(classpathDirectoryEntries));
      } catch (IOException e) {
        throw new ProjectBuilderException(
            "Could not traverse classpath directory of function bundle", e);
      }
    }

    return Optional.empty();
  }
}
