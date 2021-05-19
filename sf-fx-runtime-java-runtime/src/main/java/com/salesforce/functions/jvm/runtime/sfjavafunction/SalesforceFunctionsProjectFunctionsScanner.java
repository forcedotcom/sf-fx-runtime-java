/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction;

import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectFunctionsScanner;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.SalesforceFunctionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.FunctionResultMarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.FunctionResultMarshallers;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.PayloadUnmarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.PayloadUnmarshallers;
import com.salesforce.functions.jvm.runtime.sfjavafunction.sdk.SdkDetector;
import com.salesforce.functions.jvm.runtime.sfjavafunction.sdk.SdkFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.sdk.SdkLogic;
import com.salesforce.functions.jvm.runtime.util.JarFileUtils;
import io.cloudevents.CloudEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProjectFunctionsScanner implementation that scans the classpath for user functions that comply to
 * the Salesforce Functions Java API. It will return functions wrapped in a {@link
 * SalesforceFunction} that accepts a {@link CloudEvent} as the sole input argument and transforms
 * it accordingly.
 *
 * <p>This functions scanner will log issues with functions that look like they should be compatible
 * but aren't for whatever reason. Customers can use these error messages to fix mistakes in their
 * function definition.
 */
public class SalesforceFunctionsProjectFunctionsScanner
    implements ProjectFunctionsScanner<
        SalesforceFunction, CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(SalesforceFunctionsProjectFunctionsScanner.class);

  @Override
  public List<SalesforceFunction> scan(Project project) {
    Objects.requireNonNull(project);

    // We inject a slf4j compatible logger binding into the project classloader in the next
    // step. To do so, we need to get the implementation JAR out of the invoker JAR:
    Path loggerJarPath;
    try {
      Optional<Path> optionalLoggerJarPath =
          JarFileUtils.copyJarFileFromClassPath(
              getClass().getClassLoader(), "sf-fx-java-logger.jar");
      if (!optionalLoggerJarPath.isPresent()) {
        LOGGER.error("Could not find logger implementation JAR!");
        return Collections.emptyList();
      }

      loggerJarPath = optionalLoggerJarPath.get();
    } catch (IOException e) {
      LOGGER.error("Could not copy logger binding JAR file to disk!", e);
      return Collections.emptyList();
    }

    // Class loader for the user project, does only contain classes (including dependencies) from
    // the the user-defined project and bootstrap classes. Specifically no SDK implementation! This
    // ensures no classes leak from the runtime to the function. The only exception is the injected
    // logger JAR file since it needs to be visible for the classes inside the project.
    final ClassLoader projectClassLoader = project.createClassLoader(loggerJarPath);

    // Class loader that exposes a subset of classes from the general runtime class loader. This is
    // necessary to share some classes between the SDK (see below) and the runtime.
    //
    // The SDK needs to be initialized with a CloudEvent and the required Salesforce extensions. To
    // be able to pass those objects to the SDK constructor/initializer, both the runtime and the
    // SDK class loader need to use the same class loaded from the same class loader.
    final AllowListClassLoader allowListClassLoader =
        new AllowListClassLoader(
            // Use the classloader from this class which should be the regular classloader for the
            // runtime.
            this.getClass().getClassLoader(),
            // Parent class loader
            projectClassLoader,
            "io.cloudevents.",
            "com.salesforce.functions.jvm.runtime.cloudevent.");

    final SdkLogic sdkLogic;
    try {
      sdkLogic = SdkDetector.detect(allowListClassLoader).orElse(null);
      if (sdkLogic == null) {
        LOGGER.error("Could not detect SDK in functions project!");
        return Collections.emptyList();
      }
    } catch (IOException e) {
      LOGGER.error("Unexpected IOException while detecting SDK version in functions project!", e);
      return Collections.emptyList();
    }

    List<SdkFunction> scannedFunctions = sdkLogic.scan();
    List<SalesforceFunction> foundFunctions = new ArrayList<>();

    for (SdkFunction scannedFunction : scannedFunctions) {
      // Determine PayloadUnmarshaller for this user function
      final PayloadUnmarshaller unmarshaller;
      try {
        unmarshaller =
            PayloadUnmarshallers.forTypeString(
                scannedFunction.getInputType().toString(), projectClassLoader);
      } catch (Throwable e) {
        LOGGER.warn(
            "Function {} declares an unsupported payload type '{}': {}",
            scannedFunction.getClassName(),
            scannedFunction.getInputType().toString(),
            e.getMessage());
        continue;
      }

      // Determine FunctionResultMarshaller for this user function
      final FunctionResultMarshaller marshaller;
      try {
        marshaller =
            FunctionResultMarshallers.forTypeString(
                scannedFunction.getReturnType().toString(), projectClassLoader);
      } catch (ClassNotFoundException | AmbiguousJsonLibraryException e) {
        LOGGER.warn(
            "Function {} declares an unsupported return type '{}': {}!",
            scannedFunction.getClassName(),
            scannedFunction.getReturnType(),
            e.getMessage());
        continue;
      }

      foundFunctions.add(
          new SalesforceFunction(
              unmarshaller,
              marshaller,
              scannedFunction.getClassName(),
              new Slf4j1MdcDataInvocableWrapper(
                  allowListClassLoader, scannedFunction.getInvocationWrapper())));
    }

    return foundFunctions;
  }
}
