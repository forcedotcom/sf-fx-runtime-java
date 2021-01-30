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
