/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import com.salesforce.functions.jvm.runtime.InvocationInterface;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.SalesforceFunctionException;
import io.cloudevents.CloudEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServeCommandImpl implements Callable<Integer> {
  private final Path projectPath;
  private final List<ProjectBuilder> projectBuilders;
  private final InvocationInterface<
          CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
      invocationInterface;

  private static final Logger LOGGER = LoggerFactory.getLogger(ServeCommandImpl.class);

  public ServeCommandImpl(
      Path projectPath,
      List<ProjectBuilder> projectBuilders,
      InvocationInterface<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
          invocationInterface) {

    this.projectPath = projectPath;
    this.projectBuilders = projectBuilders;
    this.invocationInterface = invocationInterface;
  }

  @Override
  public Integer call() throws Exception {
    DetectionResult result = Detector.detect(projectPath, projectBuilders);
    List<SalesforceFunction> functions = result.getFunctions();

    functions.forEach(function -> LOGGER.info("Found function: {}", function.getName()));

    ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> function =
        functions.get(0);

    invocationInterface.start(function);
    return ExitCodes.SUCCESS;
  }
}
