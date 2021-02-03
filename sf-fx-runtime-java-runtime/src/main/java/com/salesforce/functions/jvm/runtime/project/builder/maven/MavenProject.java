/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.project.builder.maven;

import com.salesforce.functions.jvm.runtime.project.Project;

import java.nio.file.Path;
import java.util.List;

public final class MavenProject extends Project {
    private final List<Path> classpathPaths;

    public MavenProject(List<Path> classpathPaths) {
        this.classpathPaths = classpathPaths;
    }

    @Override
    public String getTypeName() {
        return "Maven";
    }

    @Override
    public List<Path> getClasspathPaths() {
        return this.classpathPaths;
    }
}
