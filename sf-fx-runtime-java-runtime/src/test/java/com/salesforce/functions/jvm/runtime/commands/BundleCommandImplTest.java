/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.commands;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.test.Util;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BundleCommandImplTest extends StdOutAndStdErrCapturingTest {
  @Rule public TemporaryFolder projectDirectoryFolder = new TemporaryFolder();
  @Rule public TemporaryFolder bundleDirectoryFolder = new TemporaryFolder();
  private final Path sdkJarPath;

  public BundleCommandImplTest() throws IOException {
    this.sdkJarPath =
        Util.downloadFileToTemporary(
            "https://repo1.maven.org/maven2/com/salesforce/functions/sf-fx-sdk-java/1.0.0/sf-fx-sdk-java-1.0.0.jar");
  }

  @Before
  public void before() throws IOException {
    File projectTomlFile = projectDirectoryFolder.newFile("project.toml");
    Files.copy(
        Paths.get("src", "test", "resources", "default-test-project.toml"),
        projectTomlFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);
  }

  @Test
  public void testFailureBundleDirectoryNotADirectory() throws Exception {
    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-string-reverse-function"));

    Project mockedProject = mock(Project.class);
    when(mockedProject.getClasspathPaths()).thenReturn(paths);

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    Path bundleDirectoryFilePath = bundleDirectoryFolder.newFile().toPath();

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFilePath,
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.BUNDLE_DIRECTORY_NOT_A_DIRECTORY));
    assertThat(
        systemOutContent.toString(),
        containsString("Bundle path " + bundleDirectoryFilePath + " must be an empty directory!"));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testFailureBundleDirectoryNotEmpty() throws Exception {
    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-string-reverse-function"));

    Project mockedProject = mock(Project.class);
    when(mockedProject.getClasspathPaths()).thenReturn(paths);

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder));

    Files.write(
        bundleDirectoryFolder.getRoot().toPath().resolve("file.txt"),
        "Hello World!".getBytes(StandardCharsets.UTF_8));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.BUNDLE_DIRECTORY_NOT_EMPTY));
    assertThat(
        systemOutContent.toString(),
        containsString(
            "Bundle path " + bundleDirectoryFolder.getRoot().toPath() + " must be empty!"));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testSuccessWithSingleFunction() throws Exception {
    Project mockedProject = mock(Project.class);

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-string-reverse-function"));

    when(mockedProject.getClasspathPaths()).thenReturn(paths);

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.SUCCESS));
    assertThat(systemOutContent.toString(), containsString("Found 1 function(s) after"));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
  }

  @Test
  public void testSuccessWithSingleFunctionAndNonExistingBundleDirectory() throws Exception {
    Project mockedProject = mock(Project.class);

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-string-reverse-function"));

    when(mockedProject.getClasspathPaths()).thenReturn(paths);

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    Path bundleDirectoryPath = bundleDirectoryFolder.getRoot().toPath().resolve("inner");

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryPath,
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.SUCCESS));
    assertThat(systemOutContent.toString(), containsString("Found 1 function(s) after"));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
  }

  @Test
  public void testFailureMultipleFunction() throws Exception {
    Project mockedProject = mock(Project.class);

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-multiple-functions"));

    when(mockedProject.getClasspathPaths()).thenReturn(paths);

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.MULTIPLE_FUNCTIONS_FOUND));
    assertThat(systemOutContent.toString(), containsString("Found 2 function(s) after"));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
  }

  @Test
  public void testFailureNoProjectFound() throws Exception {
    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.empty());

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.NO_PROJECT_FOUND));
    assertThat(systemOutContent.toString(), containsString("Could not find project at path"));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
  }

  @Test
  public void testFailureNoFunctionsFound() throws Exception {
    Project mockedProject = mock(Project.class);
    when(mockedProject.getClasspathPaths()).thenReturn(Collections.singletonList(sdkJarPath));

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.NO_FUNCTIONS_FOUND));
    assertThat(systemOutContent.toString(), containsString("Found 0 function(s) after"));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
  }

  @Test
  public void testFailureWhenUnsupportedSalesforceApiVersion() throws Exception {
    File projectTomlFile = new File(projectDirectoryFolder.getRoot(), "project.toml");
    Files.copy(
        Paths.get("src", "test", "resources", "salesforce-api-version-unsupported-project.toml"),
        projectTomlFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);

    Project mockedProject = mock(Project.class);
    when(mockedProject.getClasspathPaths()).thenReturn(Collections.singletonList(sdkJarPath));

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.UNSUPPORTED_SALESFORCE_API_VERSION));
    assertThat(
        systemOutContent.toString(),
        containsString(
            "Salesforce Rest API Version \"XYZ\" is not supported. Please change `com.salesforce.salesforce-api-version` in project.toml to \"53.0\" or newer."));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
  }

  @Test
  public void testFailureWhenSalesforceApiVersionIsLowerThanTheMinimumVersion() throws Exception {
    File projectTomlFile = new File(projectDirectoryFolder.getRoot(), "project.toml");
    Files.copy(
        Paths.get(
            "src", "test", "resources", "salesforce-api-version-less-than-minimum-project.toml"),
        projectTomlFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);

    Project mockedProject = mock(Project.class);
    when(mockedProject.getClasspathPaths()).thenReturn(Collections.singletonList(sdkJarPath));

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.UNSUPPORTED_SALESFORCE_API_VERSION));
    assertThat(
        systemOutContent.toString(),
        containsString(
            "Salesforce Rest API Version \"52.0\" is not supported. Please change `com.salesforce.salesforce-api-version` in project.toml to \"53.0\" or newer."));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
  }

  @Test
  public void testMissingSalesforceApiVersionDefaultsToMinimumVersion() throws Exception {
    File projectTomlFile = new File(projectDirectoryFolder.getRoot(), "project.toml");
    Files.copy(
        Paths.get("src", "test", "resources", "salesforce-api-version-missing-project.toml"),
        projectTomlFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);

    Project mockedProject = mock(Project.class);

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-string-reverse-function"));

    when(mockedProject.getClasspathPaths()).thenReturn(paths);

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.SUCCESS));
    assertThat(
        systemOutContent.toString(),
        containsString(
            "Project's Salesforce API version isn't explicitly defined in project.toml. The default version 53.0 will be used."));
    assertThat(systemOutContent.toString(), containsString("Found 1 function(s) after"));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
  }

  @Test
  public void testSalesforceApiVersionIsGreaterThanTheCurrentVersion() throws Exception {
    File projectTomlFile = new File(projectDirectoryFolder.getRoot(), "project.toml");
    Files.copy(
        Paths.get(
            "src", "test", "resources", "salesforce-api-version-greater-than-current-project.toml"),
        projectTomlFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);

    Project mockedProject = mock(Project.class);

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-string-reverse-function"));

    when(mockedProject.getClasspathPaths()).thenReturn(paths);

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    BundleCommandImpl bundleCommandImpl =
        new BundleCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            bundleDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder));

    assertThat(bundleCommandImpl.call(), is(ExitCodes.SUCCESS));
    assertThat(systemOutContent.toString(), containsString("Found 1 function(s) after"));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
  }
}
