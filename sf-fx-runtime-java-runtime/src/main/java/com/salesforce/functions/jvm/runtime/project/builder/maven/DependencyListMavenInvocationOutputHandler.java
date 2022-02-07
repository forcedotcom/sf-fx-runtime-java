/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.maven;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DependencyListMavenInvocationOutputHandler
    implements MavenInvocationOutputHandler<List<Path>> {
  private final List<Path> collectedDependencyPaths = new ArrayList<>();

  @Override
  public void consumeLine(String line) {
    Matcher matcher = PATTERN.matcher(line);
    if (matcher.matches()) {
      collectedDependencyPaths.add(Paths.get(matcher.group(1)));
    }
  }

  @Override
  public List<Path> getResult() {
    return Collections.unmodifiableList(collectedDependencyPaths);
  }

  private static final Pattern PATTERN =
      Pattern.compile("^\\[INFO\\]\\s+.+?:.+?:.+?:.+?:.+?:(.+?)( -- .*)?$");
}
