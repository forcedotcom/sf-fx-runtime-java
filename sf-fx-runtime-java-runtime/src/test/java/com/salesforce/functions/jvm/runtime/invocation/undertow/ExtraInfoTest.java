/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.invocation.undertow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.google.gson.Gson;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionThrewExceptionException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ExtraInfoTest {

  @Test
  public void testDefaults() {
    ExtraInfo extraInfo = new ExtraInfo();
    assertThat(extraInfo.getRequestId(), is(equalTo("n/a")));
    assertThat(extraInfo.getSource(), is(equalTo("n/a")));
    assertThat(extraInfo.getExecutionTime(), is(equalTo(Duration.ZERO)));
    assertThat(extraInfo.getStatusCode(), is(200));
    assertThat(extraInfo.isFunctionError(), is(false));
    assertThat(extraInfo.getStacktrace(), is(empty()));
  }

  @Test
  public void testWithStatusCode() {
    ExtraInfo extraInfo = new ExtraInfo();
    ExtraInfo updatedExtraInfo = extraInfo.withStatusCode(404);

    assertThat(updatedExtraInfo.getStatusCode(), is(equalTo(404)));

    assertThat(updatedExtraInfo.getRequestId(), is(equalTo(extraInfo.getRequestId())));
    assertThat(updatedExtraInfo.getSource(), is(equalTo(extraInfo.getSource())));
    assertThat(updatedExtraInfo.getExecutionTime(), is(equalTo(extraInfo.getExecutionTime())));
    assertThat(updatedExtraInfo.isFunctionError(), is(equalTo(extraInfo.isFunctionError())));
    assertThat(updatedExtraInfo.getStacktrace(), is(equalTo(extraInfo.getStacktrace())));
  }

  @Test
  public void testWithCloudEventData() {
    CloudEvent cloudEvent =
        new CloudEventBuilder()
            .withId("id-123-id")
            .withSource(URI.create("urn:source"))
            .withType("type")
            .build();

    ExtraInfo extraInfo = new ExtraInfo();
    ExtraInfo updatedExtraInfo = extraInfo.withCloudEventData(cloudEvent);

    assertThat(updatedExtraInfo.getRequestId(), is(equalTo("id-123-id")));
    assertThat(updatedExtraInfo.getSource(), is(equalTo("urn:source")));

    assertThat(updatedExtraInfo.getExecutionTime(), is(equalTo(extraInfo.getExecutionTime())));
    assertThat(updatedExtraInfo.getStatusCode(), is(equalTo(extraInfo.getStatusCode())));
    assertThat(updatedExtraInfo.isFunctionError(), is(equalTo(extraInfo.isFunctionError())));
    assertThat(updatedExtraInfo.getStacktrace(), is(equalTo(extraInfo.getStacktrace())));
  }

  @Test
  public void testWithFunctionExecutionTime() {
    ExtraInfo extraInfo = new ExtraInfo();
    ExtraInfo updatedExtraInfo = extraInfo.withFunctionExecutionTime(Duration.ofMillis(1138));

    assertThat(updatedExtraInfo.getExecutionTime(), is(equalTo(Duration.ofMillis(1138))));

    assertThat(updatedExtraInfo.getRequestId(), is(equalTo(extraInfo.getRequestId())));
    assertThat(updatedExtraInfo.getSource(), is(equalTo(extraInfo.getSource())));
    assertThat(updatedExtraInfo.getStatusCode(), is(equalTo(extraInfo.getStatusCode())));
    assertThat(updatedExtraInfo.isFunctionError(), is(equalTo(extraInfo.isFunctionError())));
    assertThat(updatedExtraInfo.getStacktrace(), is(equalTo(extraInfo.getStacktrace())));
  }

  @Test
  public void testWithFunctionExceptionDataThrowable() {
    Exception testException = new Exception("");

    ExtraInfo extraInfo = new ExtraInfo();
    ExtraInfo updatedExtraInfo = extraInfo.withFunctionExceptionData(testException);

    assertThat(updatedExtraInfo.isFunctionError(), is(true));
    assertThat(updatedExtraInfo.getStacktrace(), hasSize(testException.getStackTrace().length));

    assertThat(updatedExtraInfo.getRequestId(), is(equalTo(extraInfo.getRequestId())));
    assertThat(updatedExtraInfo.getSource(), is(equalTo(extraInfo.getSource())));
    assertThat(updatedExtraInfo.getExecutionTime(), is(equalTo(extraInfo.getExecutionTime())));
    assertThat(updatedExtraInfo.getStatusCode(), is(equalTo(extraInfo.getStatusCode())));
  }

  @Test
  public void testWithFunctionExceptionDataFunctionThrewExceptionException() {
    List<StackTraceElement> stackTraceElements = new ArrayList<>();
    stackTraceElements.add(
        new StackTraceElement("com.example.Test", "testMethod", "Test.java", 123));

    FunctionThrewExceptionException testException =
        new FunctionThrewExceptionException(new Exception(""), stackTraceElements);

    ExtraInfo extraInfo = new ExtraInfo();
    ExtraInfo updatedExtraInfo = extraInfo.withFunctionExceptionData(testException);

    assertThat(updatedExtraInfo.isFunctionError(), is(true));
    assertThat(updatedExtraInfo.getStacktrace(), is(equalTo(stackTraceElements)));

    assertThat(updatedExtraInfo.getRequestId(), is(equalTo(extraInfo.getRequestId())));
    assertThat(updatedExtraInfo.getSource(), is(equalTo(extraInfo.getSource())));
    assertThat(updatedExtraInfo.getExecutionTime(), is(equalTo(extraInfo.getExecutionTime())));
    assertThat(updatedExtraInfo.getStatusCode(), is(equalTo(extraInfo.getStatusCode())));
  }

  @Test
  public void testWithInternalExceptionData() {
    Exception testException = new Exception("");

    ExtraInfo extraInfo = new ExtraInfo();
    ExtraInfo updatedExtraInfo = extraInfo.withInternalExceptionData(testException);

    assertThat(updatedExtraInfo.isFunctionError(), is(false));
    assertThat(updatedExtraInfo.getStacktrace(), hasSize(testException.getStackTrace().length));

    assertThat(updatedExtraInfo.getRequestId(), is(equalTo(extraInfo.getRequestId())));
    assertThat(updatedExtraInfo.getSource(), is(equalTo(extraInfo.getSource())));
    assertThat(updatedExtraInfo.getExecutionTime(), is(equalTo(extraInfo.getExecutionTime())));
    assertThat(updatedExtraInfo.getStatusCode(), is(equalTo(extraInfo.getStatusCode())));
  }

  @Test
  public void testGsonSerialization() {
    ExtraInfo extraInfo =
        new ExtraInfo(
            "requestId",
            "source",
            Duration.ofMinutes(1),
            200,
            true,
            Collections.singletonList(
                new StackTraceElement("com.example.Test", "testMethod", "Test.java", 1337)));

    assertThat(
        new Gson().toJson(extraInfo),
        is(
            equalTo(
                "{\"requestId\":\"requestId\",\"source\":\"source\",\"execTimeMs\":60000,\"statusCode\":200,\"isFunctionError\":true,\"stack\":[\"com.example.Test.testMethod(Test.java:1337)\"]}")));
  }
}
