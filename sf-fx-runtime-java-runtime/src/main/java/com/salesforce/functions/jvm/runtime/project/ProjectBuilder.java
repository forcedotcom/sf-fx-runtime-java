/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.project;

import com.salesforce.functions.jvm.runtime.project.util.ProjectClassLoaderBuilder;

import java.nio.file.Path;
import java.util.Optional;

/**
 * ProjectBuilders build a Project object from a given path. Projects contain a class loader for the customers function
 * and its dependencies.
 *
 * Implementations usually make use of helper classes in core, but are not required to do so.
 *
 * @see ProjectClassLoaderBuilder
 */
public interface ProjectBuilder {
    /**
     * Builds a Project from the given path. If this ProjectBuilder cannot handle the Project, it returns an undefined
     * Optional. For example, a Gradle ProjectBuilder would return an undefined Optional when there is no build.gradle
     * file. If the builder can handle a project, but fails for any reason, a ProjectBuilderException must be thrown
     * instead.
     *
     * @param projectPath The path of the project to build.
     * @return Either a defined Optional with the built project or an undefined Optional if this builder cannot handle
     * the project at the given path.
     * @throws ProjectBuilderException If the builder could handle the project but encounters an unrecoverable error.
     */
    Optional<Project> build(Path projectPath) throws ProjectBuilderException;
}
