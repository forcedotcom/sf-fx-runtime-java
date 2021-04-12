/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.bundle;

import static com.salesforce.functions.jvm.runtime.test.Util.downloadFileToTemporary;

import com.salesforce.functions.jvm.runtime.project.Project;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FunctionBundleProjectBuilderTest {
  private Project project;

  @Before
  public void setUp() throws Exception {
    Path testFunctionBundleProjectPath = Files.createTempDirectory("test");

    Path tomlPath = testFunctionBundleProjectPath.resolve("function-bundle.toml");
    Files.createFile(tomlPath);

    Path classpathPath = testFunctionBundleProjectPath.resolve("classpath");
    Files.createDirectories(classpathPath);

    Files.move(
        downloadFileToTemporary(
            "https://repo1.maven.org/maven2/org/slf4j/slf4j-log4j12/1.7.30/slf4j-log4j12-1.7.30.jar"),
        classpathPath.resolve("slf4j-log4j12-1.7.30.jar"));

    Files.move(
        downloadFileToTemporary(
            "https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar"),
        classpathPath.resolve("logback-classic-1.2.3.jar"));

    project = new FunctionBundleProjectBuilder().build(testFunctionBundleProjectPath).get();
  }

  @Test
  public void testProjectName() {
    Assert.assertEquals("Function Bundle", project.getTypeName());
  }

  @Test
  public void testClassLoaderLoadsClasses() throws Exception {
    ClassLoader classLoader = project.createClassLoader();
    Assert.assertNotNull(classLoader.loadClass("ch.qos.logback.classic.Level"));
    Assert.assertNotNull(classLoader.loadClass("org.slf4j.impl.VersionUtil"));
  }

  @Test
  public void testClassLoaderSharesBootstrapClassLoader() throws Exception {
    ClassLoader classLoader = project.createClassLoader();
    Assert.assertSame(String.class, classLoader.loadClass("java.lang.String"));
  }

  @Test(expected = ClassNotFoundException.class)
  public void testClassLoaderIsolated() throws Exception {
    ClassLoader classLoader = project.createClassLoader();
    classLoader.loadClass("org.junit.Assert");
  }

  @Test
  public void testFailure() throws Exception {
    Optional<Project> optionalProject = new FunctionBundleProjectBuilder().build(Paths.get("."));
    Assert.assertSame(Optional.empty(), optionalProject);
  }
}
