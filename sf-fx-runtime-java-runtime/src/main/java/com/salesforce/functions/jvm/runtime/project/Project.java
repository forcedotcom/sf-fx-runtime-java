/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.project;

import com.salesforce.functions.jvm.runtime.project.util.ProjectClassLoaderBuilder;

import java.nio.file.Path;
import java.util.List;

public abstract class Project {
    abstract public String getTypeName();
    abstract public List<Path> getClasspathPaths();
    private ClassLoader classLoader;

    public ClassLoader getClassLoader() {
        if (this.classLoader == null) {
            classLoader = ProjectClassLoaderBuilder.build(getClasspathPaths());
        }

        return classLoader;
    }
}
