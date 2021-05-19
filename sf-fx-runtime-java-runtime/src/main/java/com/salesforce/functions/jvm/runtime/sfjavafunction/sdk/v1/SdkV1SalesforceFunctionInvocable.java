/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.sdk.v1;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionsInvocable;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionThrewExceptionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.SalesforceFunctionException;
import com.salesforce.functions.jvm.runtime.util.StackTraceUtils;
import io.cloudevents.CloudEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SdkV1SalesforceFunctionInvocable implements SalesforceFunctionsInvocable {
  private final Constructor<?> eventClassConstructor;
  private final Constructor<?> contextClassConstructor;

  private final Class<?> functionClass;
  private final Object functionInstance;
  private final Method functionApplyMethod;

  public SdkV1SalesforceFunctionInvocable(
      Constructor<?> eventClassConstructor,
      Constructor<?> contextClassConstructor,
      Class<?> functionClass,
      Object functionInstance,
      Method functionApplyMethod) {

    this.eventClassConstructor = eventClassConstructor;
    this.contextClassConstructor = contextClassConstructor;
    this.functionClass = functionClass;
    this.functionInstance = functionInstance;
    this.functionApplyMethod = functionApplyMethod;
  }

  @Override
  public Object invoke(
      Object payload,
      CloudEvent cloudEvent,
      SalesforceContextCloudEventExtension salesforceContext,
      SalesforceFunctionContextCloudEventExtension functionContext)
      throws SalesforceFunctionException {

    Object event;
    try {
      event = eventClassConstructor.newInstance(cloudEvent, payload);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new SalesforceFunctionException(
          String.format("Could not instantiate event class: %s", e.getMessage()), e);
    }

    Object context;
    try {
      context = contextClassConstructor.newInstance(cloudEvent, salesforceContext, functionContext);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new SalesforceFunctionException(
          String.format("Could not instantiate context class: %s", e.getMessage()), e);
    }

    try {
      return functionApplyMethod.invoke(functionInstance, event, context);
    } catch (IllegalAccessException e) {
      throw new SalesforceFunctionException(
          String.format("Could not invoke function: %s", e.getMessage()), e);

    } catch (InvocationTargetException e) {
      throw new FunctionThrewExceptionException(
          e.getCause(),
          StackTraceUtils.rebase(e.getCause().getStackTrace(), functionClass.getName()));
    }
  }
}
