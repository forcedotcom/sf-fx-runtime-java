/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.bundle;

import static org.mockito.Mockito.mock;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.sfjavafunction.InvocationWrapper;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.ByteArrayPayloadUnmarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.FunctionResultMarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.PayloadUnmarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.StringFunctionResultMarshaller;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

public class FunctionBundlerTest {
  private final InvocationWrapper mockedInvocationWrapper = mock(InvocationWrapper.class);
  private final PayloadUnmarshaller unmarshaller = new ByteArrayPayloadUnmarshaller();
  private final FunctionResultMarshaller marshaller = new StringFunctionResultMarshaller();
  private final String functionClassName = "com.example.Function";
  private final SalesforceFunction function =
      new SalesforceFunction(unmarshaller, marshaller, functionClassName, mockedInvocationWrapper);

  @Rule public TemporaryFolder temporaryBundleFolder = new TemporaryFolder();
  @Rule public TemporaryFolder temporaryClasspathFolder = new TemporaryFolder();

  private final Path fakeDependencyDirectoryPath = Paths.get("some-dependencies");
  private final Path fakeDependencyDirectory2Path = Paths.get("more-dependencies");
  private final Path fakeClassDirectoryPath = Paths.get("target", "classes");

  private final Map<Path, String> fakeClassDirectoryContents;
  private final Map<Path, String> fakeDependencyDirectoryContents;
  private final Map<Path, String> fakeDependencyDirectory2Contents;

  public FunctionBundlerTest() {
    fakeClassDirectoryContents = new HashMap<>();
    fakeClassDirectoryContents.put(Paths.get("Class1.class"), "foo");
    fakeClassDirectoryContents.put(Paths.get("Class2.class"), "bar");
    fakeClassDirectoryContents.put(Paths.get("Class3.class"), "baz");
    fakeClassDirectoryContents.put(Paths.get("com", "example", "Helper.class"), "bla");

    fakeDependencyDirectoryContents = new HashMap<>();
    fakeDependencyDirectoryContents.put(Paths.get("dependency-1.0.0.jar"), "dep-1.0.0");
    fakeDependencyDirectoryContents.put(Paths.get("other-dep-2.1.2.jar"), "other-dep-2.1.2");

    fakeDependencyDirectory2Contents = new HashMap<>();
    fakeDependencyDirectory2Contents.put(Paths.get("dependency-1.0.0.jar"), "dep-1.0.0");
  }

  private void createFakeFiles(Path directory, Map<Path, String> spec) throws IOException {
    for (Entry<Path, String> entry : spec.entrySet()) {
      Files.createDirectories(directory.resolve(entry.getKey()).getParent());

      Files.write(
          directory.resolve(entry.getKey()), entry.getValue().getBytes(StandardCharsets.UTF_8));
    }
  }

  @Test
  public void testBundling() throws IOException {
    Path temporaryClasspathPath = temporaryClasspathFolder.getRoot().toPath();

    createFakeFiles(
        temporaryClasspathPath.resolve(fakeClassDirectoryPath), fakeClassDirectoryContents);

    createFakeFiles(
        temporaryClasspathPath.resolve(fakeDependencyDirectoryPath),
        fakeDependencyDirectoryContents);

    createFakeFiles(
        temporaryClasspathPath.resolve(fakeDependencyDirectory2Path),
        fakeDependencyDirectory2Contents);

    Project project =
        new Project() {
          @Override
          public String getTypeName() {
            return "Test Project";
          }

          @Override
          public List<Path> getClasspathPaths() {
            ArrayList<Path> result = new ArrayList<>();

            // Add a directory for the class files, not the files themselves. We usually only get
            // a directory of compiled class files from build tools.
            result.add(fakeClassDirectoryPath);

            result.addAll(
                fakeDependencyDirectoryContents.keySet().stream()
                    .map(fakeDependencyDirectoryPath::resolve)
                    .collect(Collectors.toList()));

            result.addAll(
                fakeDependencyDirectory2Contents.keySet().stream()
                    .map(fakeDependencyDirectory2Path::resolve)
                    .collect(Collectors.toList()));

            return result.stream()
                .map(temporaryClasspathPath::resolve)
                .collect(Collectors.toList());
          }
        };

    FunctionBundler.bundle(project, function, temporaryBundleFolder.getRoot().toPath());

    TomlParseResult result =
        Toml.parse(temporaryBundleFolder.getRoot().toPath().resolve("function-bundle.toml"));
    Assert.assertEquals(functionClassName, result.get("function.class"));
    Assert.assertEquals("[B", result.get("function.payload_class"));
    Assert.assertEquals(
        unmarshaller.getHandledMediaType().toString(), result.get("function.payload_media_type"));
    Assert.assertEquals("java.lang.String", result.get("function.return_class"));
    Assert.assertEquals(
        marshaller.getMediaType().toString(), result.get("function.return_media_type"));

    Path classpathDirectory = temporaryBundleFolder.getRoot().toPath().resolve("classpath");

    Assert.assertTrue(classpathDirectory.toFile().isDirectory());

    Set<Path> actualFilesInClasspathDirectory =
        Files.walk(classpathDirectory, Integer.MAX_VALUE)
            .filter(Files::isRegularFile)
            .map(classpathDirectory::relativize)
            .collect(Collectors.toSet());

    Set<Path> expectedFilesInClasspathDirectory = new HashSet<>();
    expectedFilesInClasspathDirectory.addAll(
        fakeClassDirectoryContents.keySet().stream()
            .map(fakeClassFilePath -> Paths.get("classes").resolve(fakeClassFilePath))
            .collect(Collectors.toSet()));

    expectedFilesInClasspathDirectory.addAll(fakeDependencyDirectoryContents.keySet());
    expectedFilesInClasspathDirectory.addAll(fakeDependencyDirectory2Contents.keySet());

    // There are two dependency-1.0.0.jar files in different directories. We need to ensure that
    // both are copied to the classpath directory with different names:
    expectedFilesInClasspathDirectory.add(Paths.get("_dependency-1.0.0.jar"));

    Assert.assertEquals(expectedFilesInClasspathDirectory, actualFilesInClasspathDirectory);
  }
}
