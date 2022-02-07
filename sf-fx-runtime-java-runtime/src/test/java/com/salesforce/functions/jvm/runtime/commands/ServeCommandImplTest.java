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
import static org.mockito.Mockito.any;

import com.salesforce.functions.jvm.runtime.InvocationInterface;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.SalesforceFunctionException;
import com.salesforce.functions.jvm.runtime.test.Util;
import io.cloudevents.CloudEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ServeCommandImplTest extends StdOutAndStdErrCapturingTest {
  @Rule public TemporaryFolder projectDirectoryFolder = new TemporaryFolder();
  private final Path sdkJarPath;

  public ServeCommandImplTest() throws IOException {
    this.sdkJarPath =
        Util.downloadFileToTemporary(
            "https://repo1.maven.org/maven2/com/salesforce/functions/sf-fx-sdk-java/1.0.0/sf-fx-sdk-java-1.0.0.jar");
  }

  @Test
  public void testSuccessWithSingleFunction() throws Exception {
    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-string-reverse-function"));

    Project mockedProject = mock(Project.class);
    when(mockedProject.getClasspathPaths()).thenReturn(paths);

    ProjectBuilder mockedProjectBuilder = mock(ProjectBuilder.class);
    when(mockedProjectBuilder.build(projectDirectoryFolder.getRoot().toPath()))
        .thenReturn(Optional.of(mockedProject));

    InvocationInterface<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
        mockedInvocationInterface = mock(InvocationInterface.class);
    doNothing().when(mockedInvocationInterface).start(any());

    ServeCommandImpl serveCommandImpl =
        new ServeCommandImpl(
            projectDirectoryFolder.getRoot().toPath(),
            Collections.singletonList(mockedProjectBuilder),
            mockedInvocationInterface);

    assertThat(serveCommandImpl.call(), is(ExitCodes.SUCCESS));
    assertThat(systemOutContent.toString(), containsString("Found 1 function(s) after"));
    assertThat(
        systemOutContent.toString(), containsString("Found function: com.example.ExampleFunction"));
    assertThat(systemErrContent.toString(), is(emptyString()));

    verify(mockedProjectBuilder).build(projectDirectoryFolder.getRoot().toPath());
    verify(mockedInvocationInterface).start(any());
  }
}
