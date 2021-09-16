/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.google.gson.Gson;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceCloudEventExtensionParser;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.UserContext;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionThrewExceptionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.MalformedOrMissingSalesforceContextExtensionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.MalformedOrMissingSalesforceFunctionContextExtensionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.ByteArrayPayloadUnmarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.FunctionResultMarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.PayloadUnmarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.StringFunctionResultMarshaller;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SalesforceFunctionTest {
  private final InvocationWrapper mockedInvocationWrapper = mock(InvocationWrapper.class);
  private final PayloadUnmarshaller unmarshaller = new ByteArrayPayloadUnmarshaller();
  private final FunctionResultMarshaller marshaller = new StringFunctionResultMarshaller();
  private final String functionClassName = "com.example.Function";
  private final SalesforceFunction function =
      new SalesforceFunction(unmarshaller, marshaller, functionClassName, mockedInvocationWrapper);

  @Before
  public void setUp() {
    reset(mockedInvocationWrapper);
  }

  @Test
  public void testGetName() {
    assertThat(function.getName(), is(equalTo(functionClassName)));
    verify(mockedInvocationWrapper, never()).invoke(any(), any(), any(), any());
  }

  @Test
  public void testGetUnmarshaller() {
    assertThat(function.getUnmarshaller(), is(unmarshaller));
    verify(mockedInvocationWrapper, never()).invoke(any(), any(), any(), any());
  }

  @Test
  public void testGetMarshaller() {
    assertThat(function.getMarshaller(), is(marshaller));
    verify(mockedInvocationWrapper, never()).invoke(any(), any(), any(), any());
  }

  @Test
  public void testBasicApply() {
    when(mockedInvocationWrapper.invoke(any(), any(), any(), any()))
        .thenReturn(FUNCTION_RESULT_OBJECT);

    SalesforceFunctionResult result = function.apply(CLOUD_EVENT);
    assertThat(result.getData(), is(equalTo(FUNCTION_RESULT_BYTES)));

    verify(mockedInvocationWrapper)
        .invoke(
            eq(FUNCTION_INPUT_BYTES),
            eq(CLOUD_EVENT),
            eq(CONTEXT_EXTENSION),
            eq(FUNCTION_CONTEXT_EXTENSION));
  }

  @Test
  public void testApplyWithoutContextExtension() {
    when(mockedInvocationWrapper.invoke(any(), any(), any(), any()))
        .thenReturn(FUNCTION_RESULT_OBJECT);

    try {
      function.apply(CLOUD_EVENT_WITH_FUNCTION_CONTEXT_EXTENSION);
      Assert.fail("Expected MalformedOrMissingSalesforceContextExtensionException!");
    } catch (MalformedOrMissingSalesforceContextExtensionException e) {
      // This is the exception we expect, no nothing.
    }

    verify(mockedInvocationWrapper, Mockito.never()).invoke(any(), any(), any(), any());
  }

  @Test
  public void testApplyWithoutFunctionContextExtension() {
    when(mockedInvocationWrapper.invoke(any(), any(), any(), any()))
        .thenReturn(FUNCTION_RESULT_OBJECT);

    try {
      function.apply(CLOUD_EVENT_WITH_CONTEXT_EXTENSION);
      Assert.fail("Expected MalformedOrMissingSalesforceFunctionContextExtensionException!");
    } catch (MalformedOrMissingSalesforceFunctionContextExtensionException e) {
      // This is the exception we expect, no nothing.
    }

    verify(mockedInvocationWrapper, Mockito.never()).invoke(any(), any(), any(), any());
  }

  @Test
  public void testApplyWithExceptionFromInvocationHandler() {
    when(mockedInvocationWrapper.invoke(any(), any(), any(), any()))
        .thenThrow(
            new FunctionThrewExceptionException(
                new RuntimeException("Exception from function"), null));

    try {
      function.apply(CLOUD_EVENT);
      Assert.fail("Expected FunctionThrewExceptionException!");
    } catch (FunctionThrewExceptionException e) {
      Assert.assertEquals("Exception from function", e.getCause().getMessage());
    }

    verify(mockedInvocationWrapper)
        .invoke(
            eq(FUNCTION_INPUT_BYTES),
            eq(CLOUD_EVENT),
            eq(CONTEXT_EXTENSION),
            eq(FUNCTION_CONTEXT_EXTENSION));
  }

  private static final SalesforceFunctionContextCloudEventExtension FUNCTION_CONTEXT_EXTENSION =
      new SalesforceFunctionContextCloudEventExtension(
          "accessToken",
          "functionInvocationId",
          "functionName",
          "apexId",
          "apexFQN",
          "requestId",
          "resource");

  private static final SalesforceContextCloudEventExtension CONTEXT_EXTENSION =
      new SalesforceContextCloudEventExtension(
          "50.0",
          "1",
          new UserContext(
              "orgId",
              "userId",
              "onBehalfOdUserId",
              "username",
              URI.create("http://example.com"),
              URI.create("http://example.com")));

  private static final String FUNCTION_INPUT_OBJECT = "Hello!";
  private static final byte[] FUNCTION_INPUT_BYTES =
      FUNCTION_INPUT_OBJECT.getBytes(StandardCharsets.UTF_8);

  private static final String FUNCTION_RESULT_OBJECT = "Function Result!";
  private static final byte[] FUNCTION_RESULT_BYTES =
      new Gson().toJson(FUNCTION_RESULT_OBJECT).getBytes(StandardCharsets.UTF_8);

  private static final CloudEvent CLOUD_EVENT_WITHOUT_EXTENSIONS =
      new CloudEventBuilder()
          .withId("id")
          .withType("type")
          .withSource(URI.create("urn:source"))
          .withData(FUNCTION_INPUT_BYTES)
          .build();

  private static final CloudEvent CLOUD_EVENT_WITH_CONTEXT_EXTENSION =
      new CloudEventBuilder(CLOUD_EVENT_WITHOUT_EXTENSIONS)
          .withExtension(
              "sfcontext",
              SalesforceCloudEventExtensionParser.serializeSalesforceContextCloudEventExtension(
                  CONTEXT_EXTENSION))
          .build();

  private static final CloudEvent CLOUD_EVENT_WITH_FUNCTION_CONTEXT_EXTENSION =
      new CloudEventBuilder(CLOUD_EVENT_WITHOUT_EXTENSIONS)
          .withExtension(
              "sffncontext",
              SalesforceCloudEventExtensionParser.serializeSalesforceFunctionContext(
                  FUNCTION_CONTEXT_EXTENSION))
          .build();

  private static final CloudEvent CLOUD_EVENT =
      new CloudEventBuilder(CLOUD_EVENT_WITHOUT_EXTENSIONS)
          .withExtension(
              "sfcontext",
              SalesforceCloudEventExtensionParser.serializeSalesforceContextCloudEventExtension(
                  CONTEXT_EXTENSION))
          .withExtension(
              "sffncontext",
              SalesforceCloudEventExtensionParser.serializeSalesforceFunctionContext(
                  FUNCTION_CONTEXT_EXTENSION))
          .build();
}
