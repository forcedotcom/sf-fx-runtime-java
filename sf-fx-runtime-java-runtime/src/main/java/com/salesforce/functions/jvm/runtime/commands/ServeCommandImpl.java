/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import com.salesforce.functions.jvm.runtime.InvocationInterface;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.SalesforceFunctionException;
import io.cloudevents.CloudEvent;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServeCommandImpl extends AbstractDetectorCommandImpl {
  private final InvocationInterface<
          CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
      invocationInterface;

  private static final Logger LOGGER = LoggerFactory.getLogger(ServeCommandImpl.class);

  public ServeCommandImpl(
      Path projectPath,
      List<ProjectBuilder> projectBuilders,
      InvocationInterface<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
          invocationInterface) {

    super(projectPath, projectBuilders);
    this.invocationInterface = invocationInterface;
  }

  @Override
  protected Integer handle(Project project, List<SalesforceFunction> functions) throws Exception {
    functions.forEach(function -> LOGGER.info("Found function: {}", function.getName()));

    ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> function =
        functions.get(0);

    invocationInterface.start(function);
    invocationInterface.block();

    return ExitCodes.SUCCESS;
  }
}
