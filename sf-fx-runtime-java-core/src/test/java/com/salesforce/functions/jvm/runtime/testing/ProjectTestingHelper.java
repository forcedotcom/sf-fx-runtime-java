package com.salesforce.functions.jvm.runtime.testing;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectClassLoaderBuilder;

import java.nio.file.Path;
import java.util.List;

public final class ProjectTestingHelper {
    public static Project createProjectFromPaths(List<Path> paths) {
        ClassLoader classLoader = ProjectClassLoaderBuilder.build(paths);

        return new Project() {
            @Override
            public String getTypeName() {
                return "Testing";
            }

            @Override
            public ClassLoader getClassLoader() {
                return classLoader;
            }
        };
    }
}
