package com.salesforce.functions.jvm.runtime.project;

import com.salesforce.functions.jvm.runtime.testing.ProjectTestingHelper;
import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectFunctionsScannerTest {

    @Test
    public void testSingleUnaryFunction() throws Exception {
        List<Path> paths = new ArrayList<>();
        paths.add(Paths.get("src", "test", "resources", "precompiled-classes", "to-uppercase-function"));

        Project testingProject = ProjectTestingHelper.createProjectFromPaths(paths);
        List<ProjectFunction> scanResult = ProjectFunctionsScanner.scan(testingProject);

        assertEquals(1, scanResult.size());

        ProjectFunction projectFunction = scanResult.get(0);

        assertSame(testingProject, projectFunction.getProject());
        assertEquals("com.example.ToUpperCaseFunction", projectFunction.getName());
        assertEquals(1, projectFunction.getArity());
        assertEquals(Collections.singletonList(String.class), projectFunction.getParameterTypes());
        assertEquals(String.class, projectFunction.getReturnType());

        Object applyResult = projectFunction.apply("Hello World!");
        assertEquals("HELLO WORLD!", applyResult);
    }

    @Test
    public void testSingleUnaryFunctionInJarFile() throws Exception {
        List<Path> paths = new ArrayList<>();
        paths.add(Paths.get("src", "test", "resources", "squared-function.jar"));

        Project testingProject = ProjectTestingHelper.createProjectFromPaths(paths);
        List<ProjectFunction> scanResult = ProjectFunctionsScanner.scan(testingProject);

        assertEquals(1, scanResult.size());

        ProjectFunction projectFunction = scanResult.get(0);

        assertSame(testingProject, projectFunction.getProject());
        assertEquals("com.example.SquaredFunction", projectFunction.getName());
        assertEquals(1, projectFunction.getArity());
        assertEquals(Collections.singletonList(Integer.class), projectFunction.getParameterTypes());
        assertEquals(Integer.class, projectFunction.getReturnType());

        Object applyResult = projectFunction.apply(10);
        assertEquals(100, applyResult);
    }

    @Test
    public void testSingleBinaryFunctionInJarFile() throws Exception {
        List<Path> paths = new ArrayList<>();
        paths.add(Paths.get("src", "test", "resources", "string-repeat-function.jar"));

        Project testingProject = ProjectTestingHelper.createProjectFromPaths(paths);
        List<ProjectFunction> scanResult = ProjectFunctionsScanner.scan(testingProject);

        assertEquals(1, scanResult.size());

        ProjectFunction projectFunction = scanResult.get(0);

        assertSame(testingProject, projectFunction.getProject());
        assertEquals("com.example.StringRepeatFunction", projectFunction.getName());
        assertEquals(2, projectFunction.getArity());

        List<Class<?>> expectedParameterTypes = new ArrayList<>();
        expectedParameterTypes.add(String.class);
        expectedParameterTypes.add(Integer.class);
        assertEquals(expectedParameterTypes, projectFunction.getParameterTypes());

        assertEquals(String.class, projectFunction.getReturnType());

        Object applyResult = projectFunction.apply("Na", 3);
        assertEquals("NaNaNa", applyResult);
    }

    @Test
    public void testMultipleUnaryFunctions() throws Exception {
        List<Path> paths = new ArrayList<>();
        paths.add(Paths.get("src", "test", "resources", "precompiled-classes", "to-uppercase-function"));
        paths.add(Paths.get("src", "test", "resources", "squared-function.jar"));

        Project testingProject = ProjectTestingHelper.createProjectFromPaths(paths);
        List<ProjectFunction> scanResult = ProjectFunctionsScanner.scan(testingProject);

        assertEquals(2, scanResult.size());
    }
}
