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
