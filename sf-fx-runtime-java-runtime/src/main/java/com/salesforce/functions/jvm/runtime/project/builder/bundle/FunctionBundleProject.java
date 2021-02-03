/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.project.builder.bundle;

import com.salesforce.functions.jvm.runtime.project.Project;

import java.nio.file.Path;
import java.util.List;

public class FunctionBundleProject extends Project {
    private final List<Path> classpathPaths;

    public FunctionBundleProject(List<Path> classpathPaths) {
        this.classpathPaths = classpathPaths;
    }

    @Override
    public String getTypeName() {
        return "Function Bundle";
    }

    @Override
    public List<Path> getClasspathPaths() {
        return classpathPaths;
    }
}
