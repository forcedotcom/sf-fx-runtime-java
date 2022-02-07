/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.maven;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class MavenInvokerTest {
  @Test
  public void testWithoutMavenWrapper() throws MavenInvocationException {
    InvocationResult mockedInvocationResult = mock(InvocationResult.class);
    when(mockedInvocationResult.getExitCode()).thenReturn(0);

    Invoker mockedInvoker = mock(Invoker.class);
    when(mockedInvoker.execute(any())).thenReturn(mockedInvocationResult);

    MavenInvocationOutputHandler<?> mockedOutputHandler = mock(MavenInvocationOutputHandler.class);

    Path projectPath = Paths.get("/tmp/project");
    Properties properties = new Properties();

    MavenInvoker invoker = new MavenInvoker(mockedInvoker);
    invoker.invoke(projectPath, "compile", properties, mockedOutputHandler);

    verify(mockedInvoker, never()).setMavenExecutable(any());
    verify(mockedInvoker, never()).setMavenHome(any());

    ArgumentCaptor<InvocationRequest> invocationRequestArgumentCaptor =
        ArgumentCaptor.forClass(InvocationRequest.class);
    verify(mockedInvoker).execute(invocationRequestArgumentCaptor.capture());

    InvocationRequest invocationRequest = invocationRequestArgumentCaptor.getValue();

    assertThat(invocationRequest.isBatchMode(), is(true));
    assertThat(invocationRequest.getGoals(), hasItems("compile"));
    assertThat(invocationRequest.getProperties(), is(equalTo(properties)));
    assertThat(
        invocationRequest.getPomFile().toPath(), is(equalTo(projectPath.resolve("pom.xml"))));
  }

  @Test
  public void testWithMavenWrapper() throws MavenInvocationException, IOException {
    InvocationResult mockedInvocationResult = mock(InvocationResult.class);
    when(mockedInvocationResult.getExitCode()).thenReturn(0);

    Invoker mockedInvoker = mock(Invoker.class);
    when(mockedInvoker.execute(any())).thenReturn(mockedInvocationResult);

    MavenInvocationOutputHandler<?> mockedOutputHandler = mock(MavenInvocationOutputHandler.class);

    Path projectPath = Files.createTempDirectory("test-with-maven-wrapper");
    Files.createFile(projectPath.resolve("mvnw"));

    Properties properties = new Properties();

    MavenInvoker invoker = new MavenInvoker(mockedInvoker);
    invoker.invoke(projectPath, "compile", properties, mockedOutputHandler);

    verify(mockedInvoker).setMavenExecutable(projectPath.resolve("mvnw").toFile());
    verify(mockedInvoker).setMavenHome(projectPath.toFile());

    ArgumentCaptor<InvocationRequest> invocationRequestArgumentCaptor =
        ArgumentCaptor.forClass(InvocationRequest.class);
    verify(mockedInvoker).execute(invocationRequestArgumentCaptor.capture());

    InvocationRequest invocationRequest = invocationRequestArgumentCaptor.getValue();

    assertThat(invocationRequest.isBatchMode(), is(true));
    assertThat(invocationRequest.getGoals(), hasItems("compile"));
    assertThat(invocationRequest.getProperties(), is(equalTo(properties)));
    assertThat(
        invocationRequest.getPomFile().toPath(), is(equalTo(projectPath.resolve("pom.xml"))));
  }

  @Test(expected = MavenInvocationException.class)
  public void testExitCodeHandling() throws MavenInvocationException, IOException {
    InvocationResult mockedInvocationResult = mock(InvocationResult.class);
    when(mockedInvocationResult.getExitCode()).thenReturn(1);

    Invoker mockedInvoker = mock(Invoker.class);
    when(mockedInvoker.execute(any())).thenReturn(mockedInvocationResult);

    MavenInvoker invoker = new MavenInvoker(mockedInvoker);

    MavenInvocationOutputHandler<?> mockedOutputHandler = mock(MavenInvocationOutputHandler.class);

    invoker.invoke(
        Files.createTempDirectory("exit-code-handling"),
        "compile",
        new Properties(),
        mockedOutputHandler);
  }
}
