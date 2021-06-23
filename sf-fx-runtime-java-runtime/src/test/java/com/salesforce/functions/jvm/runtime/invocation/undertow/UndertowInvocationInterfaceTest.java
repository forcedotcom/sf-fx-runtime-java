/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.invocation.undertow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.*;
import io.cloudevents.CloudEvent;
import io.undertow.util.StatusCodes;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UndertowInvocationInterfaceTest {
  private UndertowInvocationInterface invocationInterface;
  private final OkHttpClient client = new OkHttpClient();
  private final ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
      helloWorldFunction;

  public UndertowInvocationInterfaceTest() {
    helloWorldFunction = mock(ProjectFunction.class);
    when(helloWorldFunction.getName()).thenReturn("Hello World");
    when(helloWorldFunction.apply(any()))
        .thenReturn(
            new SalesforceFunctionResult(
                MediaType.JSON_UTF_8, "\"Hello World!\"".getBytes(StandardCharsets.UTF_8)));
  }

  @Before
  public void setUp() {
    invocationInterface = new UndertowInvocationInterface(54321, "localhost");
  }

  @After
  public void tearDown() throws Exception {
    if (invocationInterface != null) {
      invocationInterface.stop();
      invocationInterface = null;
    }

    reset(helloWorldFunction);
  }

  @Test
  public void testIsStarted() throws Exception {
    assertThat(invocationInterface.isStarted(), is(equalTo(false)));
    invocationInterface.start(helloWorldFunction);
    assertThat(invocationInterface.isStarted(), is(equalTo(true)));
    invocationInterface.stop();
    assertThat(invocationInterface.isStarted(), is(equalTo(false)));
  }

  @Test
  public void testGet() throws Exception {
    invocationInterface.start(helloWorldFunction);

    Response response =
        client.newCall(new Request.Builder().url("http://localhost:54321").get().build()).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.METHOD_NOT_ALLOWED)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(response.body().string(), is(equalTo("\"HTTP 405: Method Not Allowed\"")));
  }

  @Test
  public void testInvalidPath() throws Exception {
    invocationInterface.start(helloWorldFunction);
    Response response =
        client
            .newCall(
                new Request.Builder()
                    .url("http://localhost:54321/invoke")
                    .post(RequestBody.create("{}", okhttp3.MediaType.get("application/json")))
                    .build())
            .execute();

    assertThat(response.code(), is(equalTo(StatusCodes.NOT_FOUND)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(response.body().string(), is(equalTo("\"HTTP 404: Not Found\"")));
  }

  @Test
  public void testHealthCheckTrue() throws Exception {
    invocationInterface.start(helloWorldFunction);
    Response response =
        client
            .newCall(
                new Request.Builder()
                    .url("http://localhost:54321")
                    .header("X-Health-Check", "true")
                    .post(RequestBody.create("{}", okhttp3.MediaType.get("application/json")))
                    .build())
            .execute();

    assertThat(response.code(), is(equalTo(StatusCodes.OK)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(response.body().string(), is(equalTo("\"OK\"")));
  }

  @Test
  public void testUnparseableCloudEvent() throws Exception {
    invocationInterface.start(helloWorldFunction);
    Response response =
        client
            .newCall(
                new Request.Builder()
                    .url("http://localhost:54321")
                    .header("ce-id", "foobar")
                    .post(RequestBody.create("{}", okhttp3.MediaType.get("application/json")))
                    .build())
            .execute();

    assertThat(response.code(), is(equalTo(StatusCodes.BAD_REQUEST)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(
        response.body().string(),
        is(
            equalTo(
                "\"Could not parse CloudEvent: Could not parse. Unknown encoding. Invalid content type or spec version\"")));
  }

  @Test
  public void testHelloWorldInvocation() throws Exception {
    invocationInterface.start(helloWorldFunction);

    Response response = client.newCall(makeJsonRequest("{}")).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.OK)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(response.body().string(), is(equalTo("\"Hello World!\"")));
  }

  @Test
  public void testNonJsonFunctionInvocation() throws Exception {
    ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
        nonJsonFunction = mock(ProjectFunction.class);
    when(nonJsonFunction.getName()).thenReturn("Non JSON function");
    when(nonJsonFunction.apply(any()))
        .thenReturn(
            new SalesforceFunctionResult(
                MediaType.ANY_IMAGE_TYPE, new byte[] {0x00, 0x00, 0x00, 0x00}));

    invocationInterface.start(nonJsonFunction);

    Response response = client.newCall(makeJsonRequest("{}")).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.SERVICE_UNAVAILABLE)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(
        response.body().string(),
        is(equalTo("\"Function returned non-JSON data which is unsupported!\"")));
  }

  @Test
  public void testMissingSalesforceContextExtensionInvocation() throws Exception {
    invocationInterface.start(
        makeThrowingFunctionMock(new MalformedOrMissingSalesforceContextExtensionException()));

    Response response = client.newCall(makeJsonRequest("{}")).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.BAD_REQUEST)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(
        response.body().string(),
        is(equalTo("\"CloudEvent is missing required sfcontext extension!\"")));
  }

  @Test
  public void testMissingSalesforceFunctionContextExtensionInvocation() throws Exception {
    invocationInterface.start(
        makeThrowingFunctionMock(
            new MalformedOrMissingSalesforceFunctionContextExtensionException()));

    Response response = client.newCall(makeJsonRequest("{}")).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.BAD_REQUEST)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(
        response.body().string(),
        is(equalTo("\"CloudEvent is missing required sffncontext extension!\"")));
  }

  @Test
  public void testPayloadUnmarshallingExceptionInvocation() throws Exception {
    invocationInterface.start(
        makeThrowingFunctionMock(
            new PayloadUnmarshallingException(new IllegalStateException("Test"))));

    Response response = client.newCall(makeJsonRequest("{}")).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.BAD_REQUEST)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(response.body().string(), is(equalTo("\"Could not unmarshall payload: Test\"")));
  }

  @Test
  public void testPayloadMarshallingExceptionInvocation() throws Exception {
    invocationInterface.start(
        makeThrowingFunctionMock(
            new FunctionResultMarshallingException(
                "JSON serialization failed!", new IllegalStateException("Test"))));

    Response response = client.newCall(makeJsonRequest("{}")).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.BAD_REQUEST)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(
        response.body().string(), is(equalTo("\"Could not marshall function result: Test\"")));
  }

  @Test
  public void testSdkInitializationExceptionInvocation() throws Exception {
    invocationInterface.start(makeThrowingFunctionMock(new SdkInitializationException()));

    Response response = client.newCall(makeJsonRequest("{}")).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.SERVICE_UNAVAILABLE)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(response.body().string(), is(equalTo("\"Could not initialize SDK for function!\"")));
  }

  @Test
  public void testFunctionThrowsExceptionInvocation() throws Exception {
    List<StackTraceElement> stackTraceElements = new ArrayList<>();
    stackTraceElements.add(
        new StackTraceElement("com.salesforce.Function", "test", "Function.java", 23));

    invocationInterface.start(
        makeThrowingFunctionMock(
            new FunctionThrewExceptionException(
                new IllegalStateException("Test"), stackTraceElements)));

    Response response = client.newCall(makeJsonRequest("{}")).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.INTERNAL_SERVER_ERROR)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(
        response.body().string(),
        startsWith(
            "\"Function threw exception: java.lang.IllegalStateException (Test)\\n"
                + "java.lang.IllegalStateException: Test\\n"
                + "\\tat com.salesforce.functions.jvm.runtime.invocation.undertow.UndertowInvocationInterfaceTest.testFunctionThrowsExceptionInvocation"));
  }

  @Test
  public void testUnknownSalesforceFunctionExceptionInvocation() throws Exception {
    invocationInterface.start(
        makeThrowingFunctionMock(
            new SalesforceFunctionException(new IllegalStateException("Test"))));

    Response response = client.newCall(makeJsonRequest("{}")).execute();

    assertThat(response.code(), is(equalTo(StatusCodes.SERVICE_UNAVAILABLE)));
    assertThat(response.header("content-type"), is(equalTo("application/json")));
    assertThat(
        response.body().string(), is(equalTo("\"Unknown error while executing function: Test\"")));
  }

  private static ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
      makeThrowingFunctionMock(Throwable t) {
    ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
        functionMock = mock(ProjectFunction.class);
    when(functionMock.getName()).thenReturn("Throwing Function");
    when(functionMock.apply(any())).thenThrow(t);

    return functionMock;
  }

  private static Request makeJsonRequest(String body) {
    return makeRequest(body, "application/json");
  }

  private static Request makeRequest(String body, String contentType) {
    return new Request.Builder()
        .url("http://localhost:54321")
        .header("ce-specversion", "1.0")
        .header("ce-id", "1644e6abe39e21f0163abd2e")
        .header("ce-source", "urn:event:invoke:test")
        .header("ce-type", "com.salesforce.function.invoke.sync")
        .header("ce-time", "2021-05-31T20:20:20.297915Z")
        .header(
            "ce-sfcontext",
            "eyJhcGlWZXJzaW9uIjoiNTAuMCIsInBheWxvYWRWZXJzaW9uIjoiMC4xIiwidXNlckNvbnRleHQiOnsib3JnSWQiOiIwMER4eDAwMDAwMDZJWUoiLCJ1c2VySWQiOiIwMDV4eDAwMDAwMVg4VXoiLCJvbkJlaGFsZk9mVXNlcklkIjpudWxsLCJ1c2VybmFtZSI6InRlc3QtenFpc25mNnl0bHF2QGV4YW1wbGUuY29tIiwic2FsZXNmb3JjZUJhc2VVcmwiOiJodHRwOi8vcGlzdGFjaGlvLXZpcmdvLTEwNjMtZGV2LWVkLmxvY2FsaG9zdC5pbnRlcm5hbC5zYWxlc2ZvcmNlLmNvbTo2MTA5Iiwib3JnRG9tYWluVXJsIjoiaHR0cDovL3Bpc3RhY2hpby12aXJnby0xMDYzLWRldi1lZC5sb2NhbGhvc3QuaW50ZXJuYWwuc2FsZXNmb3JjZS5jb206NjEwOSJ9fQ")
        .header(
            "ce-sffncontext",
            "eyJhY2Nlc3NUb2tlbiI6IjAwRHh4MDAwMDAwNklZSiFBUUVBUU5SYWM1YTFoUmhoZjAySFJlZ3c0c1NadktoOW9ZLm9oZFFfYV9LNHg1ZHdBZEdlZ1dlbVhWNnBOVVZLaFpfdVkyOUZ4SUVGTE9adTBHZjlvZk1HVzBIRkxacDgiLCJmdW5jdGlvbkludm9jYXRpb25JZCI6bnVsbCwiZnVuY3Rpb25OYW1lIjoiTXlGdW5jdGlvbiIsImFwZXhDbGFzc0lkIjpudWxsLCJhcGV4Q2xhc3NGUU4iOm51bGwsInJlcXVlc3RJZCI6IjAwRHh4MDAwMDAwNklZSkVBMi00WTRXM0x3X0xrb3NrY0hkRWFaemUtLU15RnVuY3Rpb24tMjAyMC0wOS0wM1QyMDo1NjoyNy42MDg0NDRaIiwicmVzb3VyY2UiOiJodHRwOi8vZGhhZ2Jlcmctd3NsMTo4MDgwIn0")
        .post(RequestBody.create(body, okhttp3.MediaType.get(contentType)))
        .build();
  }
}
