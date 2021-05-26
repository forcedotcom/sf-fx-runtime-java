/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import java.util.List;
import java.util.Optional;

public class DetectionResult {
  private final Project project;
  private final List<SalesforceFunction> functions;

  public DetectionResult(Project project, List<SalesforceFunction> functions) {
    this.project = project;
    this.functions = functions;
  }

  public Optional<Project> getProject() {
    return Optional.ofNullable(project);
  }

  public List<SalesforceFunction> getFunctions() {
    return functions;
  }
}
