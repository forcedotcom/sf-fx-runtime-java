/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

public final class ProjectMetadataParser {

  public static Optional<ProjectMetadata> parse(String projectTomlContents) {
    return parse(Toml.parse(projectTomlContents));
  }

  public static Optional<ProjectMetadata> parse(Path projectTomlPath) throws IOException {
    return parse(Toml.parse(projectTomlPath));
  }

  private static Optional<ProjectMetadata> parse(TomlParseResult result) {
    if (result.hasErrors()) {
      return Optional.empty();
    }

    return Optional.of(
        new ProjectMetadata(result.getString("com.salesforce.salesforce-api-version")));
  }
}
