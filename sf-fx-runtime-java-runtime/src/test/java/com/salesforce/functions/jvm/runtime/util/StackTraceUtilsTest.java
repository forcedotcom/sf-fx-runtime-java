/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import org.junit.Test;

public class StackTraceUtilsTest {
  private final StackTraceElement[] stackTraceElements;

  @Test
  public void testStandardCase() {
    List<StackTraceElement> result =
        StackTraceUtils.rebase(stackTraceElements, "com.example.ExampleFunction");

    assertThat(
        result,
        contains(
            equalTo(
                new StackTraceElement(
                    "com.salesforce.functions.jvm.runtime.sdk.DataApiImpl",
                    "executeRequest",
                    "DataApiImpl.java",
                    188)),
            equalTo(
                new StackTraceElement(
                    "com.salesforce.functions.jvm.runtime.sdk.DataApiImpl",
                    "query",
                    "DataApiImpl.java",
                    57)),
            equalTo(
                new StackTraceElement(
                    "com.example.ExampleFunction", "doStuff", "ExampleFunction.java", 40)),
            equalTo(
                new StackTraceElement(
                    "com.example.ExampleFunction", "apply", "ExampleFunction.java", 24))));
  }

  @Test
  public void testUnknownRootClass() {
    List<StackTraceElement> result =
        StackTraceUtils.rebase(stackTraceElements, "com.example.DoesNotExist");

    assertThat(result, hasSize(stackTraceElements.length));
  }

  @Test
  public void testEmptyStackTrace() {
    List<StackTraceElement> result =
        StackTraceUtils.rebase(new StackTraceElement[0], "com.example.DoesNotExist");

    assertThat(result, is(empty()));
  }

  public StackTraceUtilsTest() {
    stackTraceElements =
        new StackTraceElement[] {
          new StackTraceElement(
              "com.salesforce.functions.jvm.runtime.sdk.DataApiImpl",
              "executeRequest",
              "DataApiImpl.java",
              188),
          new StackTraceElement(
              "com.salesforce.functions.jvm.runtime.sdk.DataApiImpl",
              "query",
              "DataApiImpl.java",
              57),
          new StackTraceElement(
              "com.example.ExampleFunction", "doStuff", "ExampleFunction.java", 40),
          new StackTraceElement("com.example.ExampleFunction", "apply", "ExampleFunction.java", 24),
          new StackTraceElement(
              "jdk.internal.reflect.NativeMethodAccessorImpl", "invoke0", null, 0),
          new StackTraceElement(
              "java.base/jdk.internal.reflect.NativeMethodAccessorImpl",
              "invoke",
              "NativeMethodAccessorImpl.java",
              62),
          new StackTraceElement(
              "java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl",
              "invoke",
              "DelegatingMethodAccessorImpl.java",
              43),
          new StackTraceElement("java.base/java.lang.reflect.Method", "invoke", "Method.java", 566),
          new StackTraceElement(
              "com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionsProjectFunctionsScanner",
              "lambda$scan$0",
              "SalesforceFunctionsProjectFunctionsScanner.java",
              363),
          new StackTraceElement(
              "com.salesforce.functions.jvm.runtime.sfjavafunction.Slf4j1MdcDataInvocationWrapper",
              "invoke",
              "Slf4j1MdcDataInvocationWrapper.java",
              79),
          new StackTraceElement(
              "com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction",
              "apply",
              "SalesforceFunction.java",
              68),
          new StackTraceElement(
              "com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction",
              "apply",
              "SalesforceFunction.java",
              31),
          new StackTraceElement(
              "com.salesforce.functions.jvm.runtime.invocation.undertow.UndertowInvocationInterface$ProjectFunctionHandler",
              "handleRequest",
              "UndertowInvocationInterface.java",
              142),
          new StackTraceElement(
              "io.undertow.server.Connectors", "executeRootHandler", "Connectors.java", 387),
          new StackTraceElement(
              "io.undertow.server.HttpServerExchange$1", "run", "HttpServerExchange.java", 841),
          new StackTraceElement(
              "org.jboss.threads.ContextClassLoaderSavingRunnable",
              "run",
              "ContextClassLoaderSavingRunnable.java",
              35),
          new StackTraceElement(
              "org.jboss.threads.EnhancedQueueExecutor",
              "safeRun",
              "EnhancedQueueExecutor.java",
              2019),
          new StackTraceElement(
              "org.jboss.threads.EnhancedQueueExecutor$ThreadBody",
              "doRunTask",
              "EnhancedQueueExecutor.java",
              1558),
          new StackTraceElement(
              "org.jboss.threads.EnhancedQueueExecutor$ThreadBody",
              "run",
              "EnhancedQueueExecutor.java",
              1423),
          new StackTraceElement("java.lang.Thread", "run", "Thread.java", 834)
        };
  }
}
