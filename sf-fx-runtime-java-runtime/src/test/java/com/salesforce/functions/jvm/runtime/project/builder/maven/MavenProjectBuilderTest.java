/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.maven;

import static com.salesforce.functions.jvm.runtime.util.matchers.PropertiesMatchers.hasPropertyAtKey;
import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.isA;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilderException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class MavenProjectBuilderTest {

  @Test
  public void testEmptyDirectory()
      throws ProjectBuilderException, MavenInvocationException, IOException {

    Invoker mockInvoker = mock(Invoker.class);
    InvocationResult mockInvocationResult = mock(InvocationResult.class);

    when(mockInvoker.execute(any())).thenReturn(mockInvocationResult);
    when(mockInvocationResult.getExitCode()).thenReturn(0);

    MavenProjectBuilder mavenProjectBuilder =
        new MavenProjectBuilder(new MavenInvoker(mockInvoker));

    Optional<Project> result = mavenProjectBuilder.build(Files.createTempDirectory(""));
    assertThat(result, is(emptyOptional()));
  }

  @Test
  public void testWithTargetClassesDirectory()
      throws ProjectBuilderException, MavenInvocationException, IOException {

    Path projectPath = Files.createTempDirectory("");
    Files.createFile(projectPath.resolve("pom.xml"));

    Path classesDirectory = projectPath.resolve("target").resolve("classes");
    Files.createDirectories(classesDirectory);

    // Setup mocks
    Invoker mockInvoker = mock(Invoker.class);
    InvocationResult mockInvocationResult = mock(InvocationResult.class);

    when(mockInvoker.execute(any())).thenReturn(mockInvocationResult);
    when(mockInvocationResult.getExitCode()).thenReturn(0);

    MavenProjectBuilder mavenProjectBuilder =
        new MavenProjectBuilder(new MavenInvoker(mockInvoker));

    // Verify result value
    assertThat(
        mavenProjectBuilder.build(projectPath),
        is(
            optionalWithValue(
                allOf(
                    hasProperty("typeName", equalTo("Maven")),
                    hasProperty("classpathPaths", hasItems(equalTo(classesDirectory)))))));

    // Verify invocation request
    ArgumentCaptor<InvocationRequest> invocationRequestArgumentCaptor =
        ArgumentCaptor.forClass(InvocationRequest.class);

    verify(mockInvoker, times(1)).execute(invocationRequestArgumentCaptor.capture());

    assertThat(
        invocationRequestArgumentCaptor.getValue(),
        allOf(
            hasProperty("goals", hasItems("dependency:list")),
            hasProperty(
                "properties",
                allOf(
                    hasPropertyAtKey("outputAbsoluteArtifactFilename", equalTo("true")),
                    hasPropertyAtKey("includeScope", equalTo("runtime"))))));
  }

  @Test
  public void testWithoutTargetClassesDirectory()
      throws ProjectBuilderException, MavenInvocationException, IOException {

    Path projectPath = Files.createTempDirectory("");
    Files.createFile(projectPath.resolve("pom.xml"));

    Path classesDirectory = projectPath.resolve("target").resolve("custom-classes-dir");

    // Setup mocks
    Invoker mockInvoker = mock(Invoker.class);

    InvocationResult mockInvocationResult = mock(InvocationResult.class);
    when(mockInvoker.execute(any())).thenReturn(mockInvocationResult);
    when(mockInvocationResult.getExitCode()).thenReturn(0);

    when(mockInvoker.setOutputHandler(isA(HelpEvaluateMavenInvocationOutputHandler.class)))
        .then(
            (answer) -> {
              HelpEvaluateMavenInvocationOutputHandler outputHandler = answer.getArgument(0);
              outputHandler.consumeLine(classesDirectory.toString());
              return answer.getMock();
            });

    MavenProjectBuilder mavenProjectBuilder =
        new MavenProjectBuilder(new MavenInvoker(mockInvoker));

    // Verify result value
    assertThat(
        mavenProjectBuilder.build(projectPath),
        is(
            optionalWithValue(
                allOf(
                    hasProperty("typeName", equalTo("Maven")),
                    hasProperty("classpathPaths", hasItems(equalTo(classesDirectory)))))));

    // Verify invocation requests
    ArgumentCaptor<InvocationRequest> invocationRequestArgumentCaptor =
        ArgumentCaptor.forClass(InvocationRequest.class);

    verify(mockInvoker, times(2)).execute(invocationRequestArgumentCaptor.capture());

    assertThat(
        invocationRequestArgumentCaptor.getAllValues().get(0),
        allOf(
            hasProperty("goals", hasItems("help:evaluate")),
            hasProperty(
                "properties",
                hasPropertyAtKey("expression", equalTo("project.build.outputDirectory")))));

    assertThat(
        invocationRequestArgumentCaptor.getAllValues().get(1),
        allOf(
            hasProperty("goals", hasItems("dependency:list")),
            hasProperty(
                "properties",
                allOf(
                    hasPropertyAtKey("outputAbsoluteArtifactFilename", equalTo("true")),
                    hasPropertyAtKey("includeScope", equalTo("runtime"))))));
  }
}
