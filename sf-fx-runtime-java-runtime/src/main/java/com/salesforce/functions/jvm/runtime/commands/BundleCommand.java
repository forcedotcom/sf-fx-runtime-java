/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.builder.maven.MavenProjectBuilder;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionsProjectFunctionsScanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "bundle", header = "Pre-bundles a function project")
public class BundleCommand implements Callable<Integer> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BundleCommand.class);

  @CommandLine.Parameters(index = "0", description = "The directory that contains the function(s)")
  private Path projectPath;

  @CommandLine.Parameters(index = "1", description = "The directory to write the bundle to")
  private Path bundlePath;

  private final List<ProjectBuilder> projectBuilders = Arrays.asList(new MavenProjectBuilder());

  @Override
  public Integer call() throws Exception {
    if (!Files.exists(bundlePath)) {
      Files.createDirectories(bundlePath);
    } else if (!Files.isDirectory(bundlePath)) {
      System.err.println("Bundle path '" + bundlePath + "' must be an empty directory!");
      return EXIT_CODE_BUNDLE_DIRECTORY_NOT_A_DIRECTORY;
    } else if (Files.list(bundlePath).count() > 0) {
      System.err.println("Bundle path '" + bundlePath + "' must be empty!");
      return EXIT_CODE_BUNDLE_DIRECTORY_NOT_EMPTY;
    }

    LOGGER.info("Detecting project type at path {}...", projectPath);
    long projectDetectStart = System.currentTimeMillis();

    Optional<Project> optionalProject = Optional.empty();
    for (ProjectBuilder builder : projectBuilders) {
      optionalProject = builder.build(projectPath);

      if (optionalProject.isPresent()) {
        break;
      }
    }

    Project project =
        optionalProject.orElseThrow(
            () ->
                new IllegalStateException(
                    String.format("Could not find project at path %s!", projectPath)));

    long projectDetectDuration = System.currentTimeMillis() - projectDetectStart;
    LOGGER.info(
        "Detected {} project at path {} after {}ms!",
        project.getTypeName(),
        projectPath,
        projectDetectDuration);

    LOGGER.info("Scanning project for functions...");
    long scanStart = System.currentTimeMillis();
    List<SalesforceFunction> functions =
        new SalesforceFunctionsProjectFunctionsScanner().scan(project);
    long scanDuration = System.currentTimeMillis() - scanStart;
    LOGGER.info("Found {} function(s) after {}ms.", functions.size(), scanDuration);

    if (functions.isEmpty()) {
      return EXIT_CODE_NO_FUNCTIONS_FOUND;
    }

    if (functions.size() > 1) {
      return EXIT_CODE_MULTIPLE_FUNCTIONS_FOUND;
    }

    SalesforceFunction function = functions.get(0);

    // Copy all dependencies from the project to the classpath directory of the function bundle
    // directory.
    // Directory existence has been validated prior.
    Path bundleClassPath = Paths.get(bundlePath.toString(), "classpath");
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
      } else if (Files.isRegularFile(dependencyPath)) {
        Files.copy(dependencyPath, destinationDependencyPath);
      } else {
        System.err.println("Unexpected file type at path " + dependencyPath + ", exiting...");
        return EXIT_CODE_UNEXPECTED_FILE_TYPE;
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

    return EXIT_CODE_SUCCESS;
  }

  private Path getPathForDestinationDirectory(Path destinationDirectoryPath, Path path) {
    Path destination =
        Paths.get(destinationDirectoryPath.toString(), path.getFileName().toString());
    while (Files.exists(destination)) {
      destination =
          Paths.get(destinationDirectoryPath.toString(), "_", path.getFileName().toString());
    }

    return destination;
  }

  private final int EXIT_CODE_SUCCESS = 0;
  private final int EXIT_CODE_NO_FUNCTIONS_FOUND = 1;
  private final int EXIT_CODE_MULTIPLE_FUNCTIONS_FOUND = 2;
  private final int EXIT_CODE_CANNOT_WRITE_BUNDLE = 3;
  private final int EXIT_CODE_BUNDLE_DIRECTORY_NOT_EMPTY = 4;
  private final int EXIT_CODE_BUNDLE_DIRECTORY_NOT_A_DIRECTORY = 5;
  private final int EXIT_CODE_UNEXPECTED_FILE_TYPE = 6;
}
