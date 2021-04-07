/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.bundle;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FunctionBundler {

  private FunctionBundler() {}

  public static void bundle(Project project, SalesforceFunction function, Path bundlePath)
      throws IOException {

    Path bundleClassPath = Paths.get(bundlePath.toString(), "classpath");
    Files.createDirectories(bundleClassPath);

    for (Path dependencyPath : project.getClasspathPaths()) {
      final Path destinationDependencyPath =
          getPathForDestinationDirectory(bundleClassPath, dependencyPath);

      if (Files.isDirectory(dependencyPath)) {
        Files.walkFileTree(
            dependencyPath,
            new SimpleFileVisitor<Path>() {
              @Override
              public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                  throws IOException {
                Files.createDirectories(
                    destinationDependencyPath.resolve(dependencyPath.relativize(dir)));
                return FileVisitResult.CONTINUE;
              }

              @Override
              public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                  throws IOException {

                Files.copy(
                    file, destinationDependencyPath.resolve(dependencyPath.relativize(file)));
                return FileVisitResult.CONTINUE;
              }
            });
      } else {
        Files.copy(dependencyPath, destinationDependencyPath);
      }
    }

    Path functionBundleTomlPath = Paths.get(bundlePath.toString(), "function-bundle.toml");

    try (PrintWriter printWriter = new PrintWriter(functionBundleTomlPath.toFile())) {
      printWriter.println("[function]");
      printWriter.printf("class = \"%s\"\n", function.getName());
      printWriter.printf(
          "payload_class = \"%s\"\n", function.getUnmarshaller().getTargetClass().getName());
      printWriter.printf(
          "payload_media_type = \"%s\"\n",
          function.getUnmarshaller().getHandledMediaType().toString());
      printWriter.printf(
          "return_class = \"%s\"\n", function.getMarshaller().getSourceClass().getName());
      printWriter.printf(
          "return_media_type = \"%s\"\n", function.getMarshaller().getMediaType().toString());
    }
  }

  private static Path getPathForDestinationDirectory(Path destinationDirectoryPath, Path path) {
    Path destination =
        Paths.get(destinationDirectoryPath.toString(), path.getFileName().toString());

    StringBuilder filenamePrefix = new StringBuilder();
    while (Files.exists(destination)) {
      filenamePrefix.append("_");
      destination =
          Paths.get(
              destinationDirectoryPath.toString(), filenamePrefix + path.getFileName().toString());
    }

    return destination;
  }
}
