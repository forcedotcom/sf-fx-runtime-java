/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.project;

import com.salesforce.functions.jvm.runtime.project.util.ProjectClassLoaderBuilder;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectClassLoaderBuilderTest {

    @Test
    public void testBootstrapClasses() throws Exception {
        ClassLoader classLoader = ProjectClassLoaderBuilder.build(Collections.emptyList());
        Class<?> stringClass = classLoader.loadClass("java.lang.String");
        Class<?> systemClass = classLoader.loadClass("java.lang.System");

        // It's important that those are the exact same class so we can interface with loaded classes via
        // bootstrap classes easily.
        assertSame(stringClass, String.class);
        assertSame(systemClass, System.class);
    }

    @Test
    public void testIsolation() throws Exception {
        String className = getClass().getCanonicalName();

        // Try to load the class with the class loader from this test to ensure it is loadable.
        Class<?> clazz = getClass().getClassLoader().loadClass(className);
        assertNotNull(clazz);

        // We cannot use 'expect' from @Test here since we need the first 'loadClass' call to succeed.
        try {
            ClassLoader projectClassLoader = ProjectClassLoaderBuilder.build(Collections.emptyList());
            projectClassLoader.loadClass(className);
        } catch(ClassNotFoundException e) {
            return;
        }

        // If we reach this point, the class could be loaded which is an error!
        Assert.fail();
    }

    @Test
    public void testClassesDirectory() throws Exception {
        List<Path> paths = new ArrayList<>();
        paths.add(Paths.get("src", "test", "resources", "precompiled-classes", "to-uppercase-function"));

        ClassLoader classLoader = ProjectClassLoaderBuilder.build(paths);
        Class<?> clazz = classLoader.loadClass("com.example.ToUpperCaseFunction");

        assertNotNull(clazz);
    }

    @Test
    public void testJarFile() throws Exception {
        List<Path> paths = new ArrayList<>();
        paths.add(Paths.get("src", "test", "resources", "squared-function.jar"));

        ClassLoader classLoader = ProjectClassLoaderBuilder.build(paths);
        Class<?> clazz = classLoader.loadClass("com.example.SquaredFunction");

        assertNotNull(clazz);
    }
}
