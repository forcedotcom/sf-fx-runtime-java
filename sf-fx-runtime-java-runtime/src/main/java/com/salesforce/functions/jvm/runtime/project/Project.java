/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.project;

import com.salesforce.functions.jvm.runtime.project.util.ProjectClassLoaderBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Project {
    abstract public String getTypeName();

    abstract public List<Path> getClasspathPaths();

    /**
     * Creates a class loader for this project. It also allows the caller to inject JAR files that do not originate from
     * project. Injected JAR files are guaranteed to come first in the class loader created by this method. The main
     * use-case for this is injecting slf4j logger bindings into the project, ensuring they are picked up before
     * any other bindings within the project.
     *
     * @param injectJarsPaths Paths to JAR files that are injected into the project class loader.
     * @return A class loader for the project and injected JAR files.
     */
    public final ClassLoader createClassLoader(Path... injectJarsPaths) {
        // Order of injectJarPaths and getClasspathPaths() is important. See Javadoc for details!
        List<Path> paths = new ArrayList<>(Arrays.asList(injectJarsPaths));
        paths.addAll(getClasspathPaths());

        return ProjectClassLoaderBuilder.build(paths);
    }
}
