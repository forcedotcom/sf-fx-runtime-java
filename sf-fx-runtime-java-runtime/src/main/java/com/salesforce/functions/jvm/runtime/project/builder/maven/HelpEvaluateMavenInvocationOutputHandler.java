/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.maven;

import java.util.Optional;
import java.util.regex.Pattern;

final class HelpEvaluateMavenInvocationOutputHandler
    implements MavenInvocationOutputHandler<Optional<String>> {
  private String evaluationResult;

  @Override
  public void consumeLine(String line) {
    if (!PATTERN.matcher(line).matches()) {
      evaluationResult = line;
    }
  }

  @Override
  public Optional<String> getResult() {
    return Optional.ofNullable(evaluationResult);
  }

  private static final Pattern PATTERN = Pattern.compile("^\\[[A-Z]+\\].*$");
}
