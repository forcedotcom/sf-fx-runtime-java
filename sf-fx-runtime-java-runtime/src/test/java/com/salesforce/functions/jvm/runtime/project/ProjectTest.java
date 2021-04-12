/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project;

import static com.salesforce.functions.jvm.runtime.test.Util.downloadFileToTemporary;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ProjectTest {

  @Test
  public void testJarOrdering() throws IOException {
    Path log4jBindingJarPath =
        downloadFileToTemporary(
            "https://repo1.maven.org/maven2/org/slf4j/slf4j-log4j12/1.7.30/slf4j-log4j12-1.7.30.jar");

    Path logbackClassicJarPath =
        downloadFileToTemporary(
            "https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar");

    Path julBindingJarPath =
        downloadFileToTemporary(
            "https://repo1.maven.org/maven2/org/slf4j/slf4j-jdk14/1.7.30/slf4j-jdk14-1.7.30.jar");

    Project project =
        new Project() {
          @Override
          public String getTypeName() {
            return "Test Project";
          }

          @Override
          public List<Path> getClasspathPaths() {
            List<Path> projectClasspathPaths = new ArrayList<>();
            projectClasspathPaths.add(logbackClassicJarPath);
            projectClasspathPaths.add(julBindingJarPath);

            return projectClasspathPaths;
          }
        };

    ClassLoader classLoader = project.createClassLoader(log4jBindingJarPath);
    List<URL> classUrlsFromClassLoader =
        Collections.list(classLoader.getResources("org/slf4j/impl/StaticLoggerBinder.class"));

    Assert.assertEquals(3, classUrlsFromClassLoader.size());
    Assert.assertTrue(
        classUrlsFromClassLoader.get(0).toString().contains(log4jBindingJarPath.toString()));
    Assert.assertTrue(
        classUrlsFromClassLoader.get(1).toString().contains(logbackClassicJarPath.toString()));
    Assert.assertTrue(
        classUrlsFromClassLoader.get(2).toString().contains(julBindingJarPath.toString()));
  }
}
