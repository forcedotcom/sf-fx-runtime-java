/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.project.builder.maven;

import org.apache.maven.shared.invoker.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;

final class MavenInvoker {

    static <A> A invoke(Path projectPath, String goal, Properties invocationRequestProperties, MavenInvocationOutputHandler<A> outputHandler) throws MavenInvocationException {
        InvocationRequest invocationRequest = new DefaultInvocationRequest();
        invocationRequest.setPomFile(projectPath.resolve("pom.xml").toFile());
        invocationRequest.setGoals(Collections.singletonList(goal));
        invocationRequest.setProperties(invocationRequestProperties);
        invocationRequest.setBatchMode(true);

        Invoker invoker = new DefaultInvoker();
        invoker.setOutputHandler(outputHandler);

        if (hasMavenWrapper(projectPath)) {
            invoker.setMavenHome(projectPath.toFile());
            invoker.setMavenExecutable(projectPath.resolve("mvnw").toAbsolutePath().toFile());
        }

        InvocationResult invocationResult = invoker.execute(invocationRequest);

        if (invocationResult.getExitCode() != 0) {
            throw new MavenInvocationException("Maven exited with non-zero exit code!");
        }

        return outputHandler.getResult();
    }

    private static boolean hasMavenWrapper(Path projectPath) {
        return Files.exists(projectPath.resolve("mvnw"));
    }

    private MavenInvoker() {}
}
