/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.SalesforceFunctionException;
import io.cloudevents.CloudEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4j1MdcDataInvocationWrapper implements InvocationWrapper {
  private final Method mdcClearMethod;
  private final Method mdcPutMethod;
  private final InvocationWrapper innerInvocationWrapper;
  private static final Logger LOGGER =
      LoggerFactory.getLogger(Slf4j1MdcDataInvocationWrapper.class);

  public Slf4j1MdcDataInvocationWrapper(
      ClassLoader classLoader, InvocationWrapper innerInvocationWrapper) {
    this.innerInvocationWrapper = innerInvocationWrapper;

    Method mdcClearMethod;
    Method mdcPutMethod;
    try {
      // We look in the class loader to make sure if a class in the tree wants to log, it can,
      // even when the user project does not declare a dependency on slf4j.
      Class<?> mdcClass = classLoader.loadClass("org.slf4j.MDC");
      mdcClearMethod = mdcClass.getMethod("clear");
      mdcPutMethod = mdcClass.getMethod("put", String.class, String.class);
    } catch (ClassNotFoundException e) {
      // It's fine to not have slf4j on the classpath since that indicates that no logging is
      // taking place in customers or SDK code anyway.
      LOGGER.debug(
          "Could not find org.slf4j.MDC in classpath, invocation context data will not be available in logger.");
      mdcClearMethod = null;
      mdcPutMethod = null;
    } catch (NoSuchMethodException e) {
      LOGGER.warn(
          "Could not find required method on org.slf4j.MDC. Invocation context data will not be available in logger.");
      mdcClearMethod = null;
      mdcPutMethod = null;
    }

    this.mdcClearMethod = mdcClearMethod;
    this.mdcPutMethod = mdcPutMethod;
  }

  @Override
  public Object invoke(
      Object payload,
      CloudEvent cloudEvent,
      SalesforceContextCloudEventExtension salesforceContext,
      SalesforceFunctionContextCloudEventExtension functionContext)
      throws SalesforceFunctionException {

    if (mdcClearMethod != null) {
      try {
        mdcClearMethod.invoke(null);
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    if (mdcPutMethod != null) {
      try {
        mdcPutMethod.invoke(null, "function-invocation-id", cloudEvent.getId());
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    return innerInvocationWrapper.invoke(payload, cloudEvent, salesforceContext, functionContext);
  }
}
