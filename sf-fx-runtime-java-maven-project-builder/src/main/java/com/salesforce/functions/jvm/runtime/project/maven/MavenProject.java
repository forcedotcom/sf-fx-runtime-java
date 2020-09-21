package com.salesforce.functions.jvm.runtime.project.maven;

import com.salesforce.functions.jvm.runtime.project.Project;

public final class MavenProject implements Project {
    private ClassLoader classLoader;

    public MavenProject(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public String getTypeName() {
        return "Maven";
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
