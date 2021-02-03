/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.commands;

import com.salesforce.functions.jvm.runtime.InvocationInterface;
import com.salesforce.functions.jvm.runtime.invocation.undertow.UndertowInvocationInterface;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.project.ProjectFunctionsScanner;
import com.salesforce.functions.jvm.runtime.project.builder.bundle.FunctionBundleProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.builder.maven.MavenProjectBuilder;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionsProjectFunctionsScanner;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.SalesforceFunctionException;
import io.cloudevents.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "serve",
        header = "Serves a function project via HTTP"
)
public class ServeCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServeCommand.class);

    @Parameters(index = "0", description = "The directory that contains the function(s)")
    private Path projectPath;

    @Option(names = {"-p", "--port"}, description = "The port the webserver should listen on.")
    private int port = 8080;

    private final List<ProjectBuilder> projectBuilders = Arrays.asList(
            new FunctionBundleProjectBuilder(),
            new MavenProjectBuilder()
    );

    @Override
    public Integer call() throws Exception {
        LOGGER.info("Detecting project type at path {}...", projectPath);
        long projectDetectStart = System.currentTimeMillis();

        Optional<Project> optionalProject = Optional.empty();
        for (ProjectBuilder builder : projectBuilders) {
            optionalProject = builder.build(projectPath);

            if (optionalProject.isPresent()) {
                break;
            }
        }

        Project project = optionalProject
                .orElseThrow(() -> new IllegalStateException(String.format("Could not find project at path %s!", projectPath)));

        long projectDetectDuration = System.currentTimeMillis() - projectDetectStart;
        LOGGER.info("Detected {} project at path {} after {}ms!", project.getTypeName(), projectPath, projectDetectDuration);

        LOGGER.info("Scanning project for functions...");
        long scanStart = System.currentTimeMillis();
        List<SalesforceFunction> functions = createFunctionScanner().scan(project);
        long scanDuration = System.currentTimeMillis() - scanStart;
        LOGGER.info("Found {} function(s) after {}ms.", functions.size(), scanDuration);

        functions.forEach(function -> LOGGER.info("Found function: {}", function.getName()));

        ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> function =
                functions.get(0);

        createInvocationInterface().start(function);
        return 0;
    }

    private ProjectFunctionsScanner<SalesforceFunction, CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> createFunctionScanner() {
        return new SalesforceFunctionsProjectFunctionsScanner();
    }

    private InvocationInterface<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> createInvocationInterface() {
        return new UndertowInvocationInterface(port);
    }
}
