/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.bundle;

import static com.salesforce.functions.jvm.runtime.test.Util.downloadFileToTemporary;
import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.salesforce.functions.jvm.runtime.project.Project;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
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
            "https://repo1.maven.org/maven2/org/tinylog/tinylog-api/2.4.1/tinylog-api-2.4.1.jar"),
        classpathPath.resolve("tinylog-1.3.6.jar"));

    Files.move(
        downloadFileToTemporary(
            "https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar"),
        classpathPath.resolve("logback-classic-1.2.3.jar"));

    project = new FunctionBundleProjectBuilder().build(testFunctionBundleProjectPath).get();
  }

  @Test
  public void testProjectName() {
    assertThat(project.getTypeName(), is(equalTo("Function Bundle")));
  }

  @Test
  public void testClassLoaderLoadsClasses() throws Exception {
    ClassLoader classLoader = project.createClassLoader();

    assertThat(classLoader.loadClass("ch.qos.logback.classic.Level"), is(notNullValue()));
    assertThat(classLoader.loadClass("org.tinylog.Logger"), is(notNullValue()));
  }

  @Test
  public void testClassLoaderSharesBootstrapClassLoader() throws Exception {
    ClassLoader classLoader = project.createClassLoader();
    assertThat(classLoader.loadClass("java.lang.String"), is(String.class));
  }

  @Test(expected = ClassNotFoundException.class)
  public void testClassLoaderIsolated() throws Exception {
    ClassLoader classLoader = project.createClassLoader();
    classLoader.loadClass("org.junit.Assert");
  }

  @Test
  public void testFailure() throws Exception {
    Optional<Project> optionalProject = new FunctionBundleProjectBuilder().build(Paths.get("."));
    assertThat(optionalProject, is(emptyOptional()));
  }
}
