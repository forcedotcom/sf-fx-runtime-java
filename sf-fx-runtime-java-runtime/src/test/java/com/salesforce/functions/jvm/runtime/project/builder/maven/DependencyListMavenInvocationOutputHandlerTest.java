/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.maven;

import com.salesforce.functions.jvm.runtime.test.Util;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class DependencyListMavenInvocationOutputHandlerTest {

  @Test
  public void testDependencyListWithModules() throws IOException {
    MavenInvocationOutputHandler<List<Path>> outputHandler =
        new DependencyListMavenInvocationOutputHandler();

    List<String> lines =
        Util.readLinesFromResource("maven-dependency-list-with-modules-output.txt");
    List<Path> expectedPaths =
        Util.readLinesFromResource("maven-dependency-list-with-modules-expected.txt").stream()
            .filter(line -> !line.isEmpty())
            .map(Paths::get)
            .collect(Collectors.toList());

    for (String line : lines) {
      outputHandler.consumeLine(line);
    }

    Assert.assertEquals(expectedPaths, outputHandler.getResult());
  }

  @Test
  public void testDependencyListWithoutModules() throws IOException {
    MavenInvocationOutputHandler<List<Path>> outputHandler =
        new DependencyListMavenInvocationOutputHandler();

    List<String> lines = Util.readLinesFromResource("maven-dependency-list-output.txt");
    List<Path> expectedPaths =
        Util.readLinesFromResource("maven-dependency-list-expected.txt").stream()
            .filter(line -> !line.isEmpty())
            .map(Paths::get)
            .collect(Collectors.toList());

    for (String line : lines) {
      outputHandler.consumeLine(line);
    }

    Assert.assertEquals(expectedPaths, outputHandler.getResult());
  }
}
