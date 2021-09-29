/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceCloudEventExtensionParser;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.UserContext;
import com.salesforce.functions.jvm.runtime.commands.StdOutAndStdErrCapturingTest;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionThrewExceptionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.ByteArrayPayloadUnmarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.JsonFunctionResultMarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.JsonPayloadUnmarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.StringFunctionResultMarshaller;
import com.salesforce.functions.jvm.runtime.test.Util;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class SalesforceFunctionsProjectFunctionsScannerTest extends StdOutAndStdErrCapturingTest {
  private final Path sdkJarPath;
  private final Path jacksonDatabindJarPath;
  private final Path jacksonCoreJarPath;
  private final Path gsonJarPath;
  private final Path jacksonAnnotationsJarPath;

  public SalesforceFunctionsProjectFunctionsScannerTest() throws IOException {
    this.sdkJarPath =
        Util.downloadFileToTemporary(
            "https://repo1.maven.org/maven2/com/salesforce/functions/sf-fx-sdk-java/1.0.0/sf-fx-sdk-java-1.0.0.jar");

    this.jacksonDatabindJarPath =
        Util.downloadFileToTemporary(
            "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.12.3/jackson-databind-2.12.3.jar");

    this.jacksonCoreJarPath =
        Util.downloadFileToTemporary(
            "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.12.3/jackson-core-2.12.3.jar");

    this.gsonJarPath =
        Util.downloadFileToTemporary(
            "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar");

    this.jacksonAnnotationsJarPath =
        Util.downloadFileToTemporary(
            "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.12.3/jackson-annotations-2.12.3.jar");
  }

  @Test
  public void testSuccessPojoInStringOutFunction() {
    SalesforceFunctionsProjectFunctionsScanner scanner =
        new SalesforceFunctionsProjectFunctionsScanner();

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-string-reverse-function"));

    Project mockProject = mock(Project.class);
    when(mockProject.getTypeName()).thenReturn("Mocked");
    when(mockProject.getClasspathPaths()).thenReturn(paths);

    List<SalesforceFunction> functions = scanner.scan(mockProject);
    assertThat(
        functions,
        contains(
            allOf(
                hasProperty("name", equalTo("com.example.ExampleFunction")),
                hasProperty("unmarshaller", instanceOf(JsonPayloadUnmarshaller.class)),
                hasProperty("marshaller", instanceOf(StringFunctionResultMarshaller.class)))));

    assertThat(
        functions
            .get(0)
            .apply(
                cloudEventWithData("{\"value\":\"hello world\"}".getBytes(StandardCharsets.UTF_8))),
        allOf(
            hasProperty("mediaType", equalTo(MediaType.JSON_UTF_8)),
            hasProperty("data", equalTo("\"dlrow olleh\"".getBytes(StandardCharsets.UTF_8)))));

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testSuccessBytesInPojoOutFunction() {
    SalesforceFunctionsProjectFunctionsScanner scanner =
        new SalesforceFunctionsProjectFunctionsScanner();

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-count-bytes-function"));

    Project mockProject = mock(Project.class);
    when(mockProject.getTypeName()).thenReturn("Mocked");
    when(mockProject.getClasspathPaths()).thenReturn(paths);

    List<SalesforceFunction> functions = scanner.scan(mockProject);
    assertThat(
        functions,
        contains(
            allOf(
                hasProperty("name", equalTo("com.example.CountBytesFunction")),
                hasProperty("unmarshaller", instanceOf(ByteArrayPayloadUnmarshaller.class)),
                hasProperty("marshaller", instanceOf(JsonFunctionResultMarshaller.class)))));

    assertThat(
        functions.get(0).apply(cloudEventWithData("1138".getBytes(StandardCharsets.UTF_8))),
        allOf(
            hasProperty("mediaType", equalTo(MediaType.JSON_UTF_8)),
            hasProperty("data", equalTo("{\"length\":4}".getBytes(StandardCharsets.UTF_8)))));

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testSuccessJoinStringListFunction() {
    SalesforceFunctionsProjectFunctionsScanner scanner =
        new SalesforceFunctionsProjectFunctionsScanner();

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-join-string-list-function"));

    Project mockProject = mock(Project.class);
    when(mockProject.getTypeName()).thenReturn("Mocked");
    when(mockProject.getClasspathPaths()).thenReturn(paths);

    List<SalesforceFunction> functions = scanner.scan(mockProject);
    assertThat(
        functions,
        contains(
            allOf(
                hasProperty("name", equalTo("com.example.JoinStringListFunction")),
                hasProperty("unmarshaller", instanceOf(JsonPayloadUnmarshaller.class)),
                hasProperty("marshaller", instanceOf(StringFunctionResultMarshaller.class)))));

    assertThat(
        functions
            .get(0)
            .apply(
                cloudEventWithData("[\"foo\", \"bar\", \"baz\"]".getBytes(StandardCharsets.UTF_8))),
        allOf(
            hasProperty("mediaType", equalTo(MediaType.JSON_UTF_8)),
            hasProperty("data", equalTo("\"foo, bar, baz\"".getBytes(StandardCharsets.UTF_8)))));

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testSuccessUppercaseListOfStringsFunction() {
    SalesforceFunctionsProjectFunctionsScanner scanner =
        new SalesforceFunctionsProjectFunctionsScanner();

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-uppercase-list-of-strings-function"));

    Project mockProject = mock(Project.class);
    when(mockProject.getTypeName()).thenReturn("Mocked");
    when(mockProject.getClasspathPaths()).thenReturn(paths);

    List<SalesforceFunction> functions = scanner.scan(mockProject);
    assertThat(
        functions,
        contains(
            allOf(
                hasProperty("name", equalTo("com.example.UppercaseListOfStringsFunction")),
                hasProperty("unmarshaller", instanceOf(JsonPayloadUnmarshaller.class)),
                hasProperty("marshaller", instanceOf(JsonFunctionResultMarshaller.class)))));

    assertThat(
        functions
            .get(0)
            .apply(
                cloudEventWithData("[\"foo\", \"bar\", \"baz\"]".getBytes(StandardCharsets.UTF_8))),
        allOf(
            hasProperty("mediaType", equalTo(MediaType.JSON_UTF_8)),
            hasProperty(
                "data", equalTo("[\"FOO\",\"BAR\",\"BAZ\"]".getBytes(StandardCharsets.UTF_8)))));

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testFailingFunction() {
    SalesforceFunctionsProjectFunctionsScanner scanner =
        new SalesforceFunctionsProjectFunctionsScanner();

    List<Path> paths = new ArrayList<>();
    paths.add(sdkJarPath);
    paths.add(Paths.get("src", "test", "resources", "sdk-1.0-always-failing-function"));

    Project mockProject = mock(Project.class);
    when(mockProject.getTypeName()).thenReturn("Mocked");
    when(mockProject.getClasspathPaths()).thenReturn(paths);

    List<SalesforceFunction> functions = scanner.scan(mockProject);
    assertThat(
        functions,
        contains(
            allOf(
                hasProperty("name", equalTo("com.example.FailingFunction")),
                hasProperty("unmarshaller", instanceOf(ByteArrayPayloadUnmarshaller.class)),
                hasProperty("marshaller", instanceOf(StringFunctionResultMarshaller.class)))));

    FunctionThrewExceptionException exception =
        assertThrows(
            FunctionThrewExceptionException.class,
            () -> functions.get(0).apply(cloudEventWithData("".getBytes(StandardCharsets.UTF_8))));

    assertThat(
        exception,
        allOf(
            hasProperty("cause", hasProperty("message", equalTo("This function always fails!"))),
            hasProperty(
                "functionStackTrace",
                contains(
                    equalTo(
                        new StackTraceElement(
                            "com.example.FailingFunction",
                            "apply",
                            "FailingFunction.java",
                            10))))));

    assertThat(systemOutContent.toString(), is(emptyString()));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  @Test
  public void testAmbiguousPojoResultFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Potential function com.example.AmbiguousJsonPojoResultFunction declares a return type with multiple JSON framework annotations. Function will be ignored!",
        "sdk-1.0-ambiguous-json-pojo-result-function",
        sdkJarPath,
        gsonJarPath,
        jacksonCoreJarPath,
        jacksonDatabindJarPath,
        jacksonAnnotationsJarPath);
  }

  @Test
  public void testAmbiguousPojoPayloadFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Potential function com.example.AmbiguousJsonPojoPayloadFunction declares an payload type with multiple JSON framework annotations. Function will be ignored!",
        "sdk-1.0-ambiguous-json-pojo-payload-function",
        sdkJarPath,
        gsonJarPath,
        jacksonCoreJarPath,
        jacksonDatabindJarPath,
        jacksonAnnotationsJarPath);
  }

  @Test
  public void testNoDefaultConstructorFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Could not find default constructor for function class com.example.NoDefaultConstructorFunction.",
        "sdk-1.0-no-default-constructor-function",
        sdkJarPath);
  }

  @Test
  public void testExceptionInConstructorFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Exception while instantiating function class com.example.ExceptionInConstructorFunction.",
        "sdk-1.0-exception-in-constructor-function",
        sdkJarPath);
  }

  @Test
  public void testPrivateConstructorFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Could not find default constructor for function class com.example.PrivateConstructorFunction.",
        "sdk-1.0-private-constructor-function",
        sdkJarPath);
  }

  @Test
  public void testAbstractClassFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Could not instantiate function class com.example.AbstractClassFunction.",
        "sdk-1.0-abstract-class-function",
        sdkJarPath);
  }

  @Test
  public void testNoSdkFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Could not load function API class com.salesforce.functions.jvm.sdk.SalesforceFunction. Please ensure your project depends on the Java Functions API.",
        "sdk-1.0-string-reverse-function");
  }

  @Test
  public void testNoApplyMethodFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Could not find implementation of apply(InvocationEvent<?>, Context) method in function class com.example.NoApplyMethodFunction.",
        "sdk-1.0-no-apply-method-function",
        sdkJarPath);
  }

  @Test
  public void testMissingPayloadClassFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Potential function com.example.MissingPayloadClassFunction declares a payload type (com.example.Payload) that cannot be found.",
        "sdk-1.0-missing-payload-class-function",
        sdkJarPath);
  }

  @Test
  public void testMissingReturnTypeClassFunction() {
    scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
        "Could not find required class definition while inspecting function class com.example.MissingReturnTypeClassFunction.",
        "sdk-1.0-missing-return-type-class-function",
        sdkJarPath);
  }

  private void scanTestFunctionDirectoryExpectErrorMessageAndNoFunctions(
      String expectedMessage, String functionDirectory, Path... classpathJarFiles) {
    List<SalesforceFunction> functions =
        scanTestFunctionDirectory(functionDirectory, classpathJarFiles);

    assertThat(functions, is(empty()));
    assertThat(systemOutContent.toString(), containsString(expectedMessage));
    assertThat(systemErrContent.toString(), is(emptyString()));
  }

  private List<SalesforceFunction> scanTestFunctionDirectory(
      String functionDirectory, Path... classpathJarFiles) {
    SalesforceFunctionsProjectFunctionsScanner scanner =
        new SalesforceFunctionsProjectFunctionsScanner();

    List<Path> paths = new ArrayList<>();
    paths.add(Paths.get("src", "test", "resources", functionDirectory));
    paths.addAll(Arrays.asList(classpathJarFiles));

    Project mockProject = mock(Project.class);
    when(mockProject.getTypeName()).thenReturn("Mocked");
    when(mockProject.getClasspathPaths()).thenReturn(paths);

    return scanner.scan(mockProject);
  }

  private static CloudEvent cloudEventWithData(byte[] data) {
    return new CloudEventBuilder()
        .withId("id")
        .withSource(URI.create("urn:foo"))
        .withType("type")
        .withExtension(
            "sffncontext",
            SalesforceCloudEventExtensionParser.serializeSalesforceFunctionContext(
                new SalesforceFunctionContextCloudEventExtension(
                    "accessToken",
                    "invocationId",
                    "functionname",
                    "apexId",
                    "apexFQN",
                    "requestId",
                    "resource")))
        .withExtension(
            "sfcontext",
            SalesforceCloudEventExtensionParser.serializeSalesforceContextCloudEventExtension(
                new SalesforceContextCloudEventExtension(
                    "53.0",
                    "1.0",
                    new UserContext(
                        "orgId",
                        "userId",
                        "onBehalfOfUserId",
                        "username",
                        URI.create("urn:baseuri"),
                        URI.create("urn:orgbase")))))
        .withData(data)
        .build();
  }
}
