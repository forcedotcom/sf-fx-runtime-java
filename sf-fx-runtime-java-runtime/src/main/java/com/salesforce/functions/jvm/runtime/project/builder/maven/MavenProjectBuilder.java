/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.project.builder.maven;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilderException;
import com.salesforce.functions.jvm.runtime.project.util.ProjectClassLoaderBuilder;
import org.apache.maven.shared.invoker.*;

import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.*;

/**
 * A ProjectBuilder for Maven 3 projects. It will invoke Maven itself to gather the required information to ensure
 * proper resolution of Maven expressions, dependency trees and directories.
 */
public final class MavenProjectBuilder implements ProjectBuilder {

    @Override
    public Optional<Project> build(Path projectPath) throws ProjectBuilderException {
        if (!isMavenProject(projectPath)) {
            return Optional.empty();
        }

        try {
            List<Path> paths = new ArrayList<>();

            Path buildOutputDirectory = resolveBuildOutputDirectory(projectPath)
                    .orElseThrow(() -> new ProjectBuilderException("Could not determine Maven build output directory!"));

            paths.add(buildOutputDirectory);
            paths.addAll(resolveDependencyPaths(projectPath));

            return Optional.of(new MavenProject(paths));
        } catch (MavenInvocationException e) {
            throw new ProjectBuilderException("Exception while invoking Maven!", e);
        }
    }

    private static List<Path> resolveDependencyPaths(Path projectPath) throws MavenInvocationException {
        Properties properties = new Properties();
        properties.setProperty("outputAbsoluteArtifactFilename", "true");
        properties.setProperty("includeScope", "runtime");

        MavenInvocationOutputHandler<List<Path>> outputHandler = new DependencyListMavenInvocationOutputHandler();
        return MavenInvoker.invoke(projectPath, "dependency:list", properties, outputHandler);
    }

    private static Optional<Path> resolveBuildOutputDirectory(Path projectPath) throws MavenInvocationException {
        // In almost all cases, the directory will be target/classes. To avoid calling Maven too often (since it takes
        // a significant amount of time, especially here because the help plugin usually needs to be downloaded first),
        // we handle this case specifically.
        Path mavenDefaultBuildOutputDirectory = projectPath.resolve("target/classes");
        if (Files.isDirectory(mavenDefaultBuildOutputDirectory)) {
            return Optional.of(mavenDefaultBuildOutputDirectory);
        }

        Properties properties = new Properties();
        properties.setProperty("expression", "project.build.outputDirectory");

        MavenInvocationOutputHandler<Optional<String>> outputHandler = new HelpEvaluateMavenInvocationOutputHandler();

        return MavenInvoker
                .invoke(projectPath, "help:evaluate", properties, outputHandler)
                .map(Paths::get);
    }

    private static boolean isMavenProject(Path projectPath) {
        return Files.isReadable(projectPath.resolve("pom.xml"));
    }
}
