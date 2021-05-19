/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.sdk.v1;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.sdk.SdkFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.sdk.SdkLogic;
import com.salesforce.functions.jvm.runtime.util.JarFileUtils;
import io.cloudevents.CloudEvent;
import io.github.classgraph.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class V1SdkLogic implements SdkLogic {
  private static final Logger LOGGER = LoggerFactory.getLogger(V1SdkLogic.class);
  private final ClassLoader sdkClassLoader;
  private final Constructor<?> eventClassConstructor;
  private final Constructor<?> contextClassConstructor;

  public V1SdkLogic(
      ClassLoader sdkClassLoader,
      Constructor<?> eventClassConstructor,
      Constructor<?> contextClassConstructor) {
    this.sdkClassLoader = sdkClassLoader;
    this.eventClassConstructor = eventClassConstructor;
    this.contextClassConstructor = contextClassConstructor;
  }

  /**
   * Relies on the given classLoader to be able to load CloudEvent SDK classes.
   *
   * @param classLoader
   * @return
   */
  public static V1SdkLogic init(ClassLoader classLoader) {
    // Class loader that can load SDK implementation classes. It will fall back to the
    // AllowListClassLoader above to load CloudEvent related classes. The class loader above that
    // one is the project class loader that contains the SDK interface required by the SDK
    // implementation. It's important that the SDK implementation does not contain the CloudEvent
    // classes nor the SDK interface. This can be achieved by using the "provided" scope in Maven
    // for the SDK implementation project.
    final ClassLoader sdkClassLoader;

    try {
      Optional<Path> optionalSdkImplementationJarPath =
          JarFileUtils.copyJarFileFromClassPath(
              V1SdkLogic.class.getClassLoader(), "sdk-impl-v0.jar");
      if (!optionalSdkImplementationJarPath.isPresent()) {
        throw new IllegalStateException("Could not find logger implementation JAR!");
      }

      sdkClassLoader =
          new URLClassLoader(
              new URL[] {optionalSdkImplementationJarPath.get().toUri().toURL()}, classLoader);

    } catch (MalformedURLException e) {
      throw new IllegalStateException(
          "Unexpected exception while preparing SDK implementation classloader!", e);
    } catch (IOException e) {
      throw new IllegalStateException("Could copy SDK implementation JAR to disk!", e);
    }

    final Constructor<?> eventClassConstructor;
    final Constructor<?> contextClassConstructor;
    try {
      Class<?> eventClass =
          sdkClassLoader.loadClass("com.salesforce.functions.jvm.runtime.sdk.InvocationEventImpl");
      eventClassConstructor = eventClass.getConstructor(CloudEvent.class, Object.class);

      Class<?> contextClass =
          sdkClassLoader.loadClass("com.salesforce.functions.jvm.runtime.sdk.ContextImpl");
      contextClassConstructor =
          contextClass.getConstructor(
              CloudEvent.class,
              SalesforceContextCloudEventExtension.class,
              SalesforceFunctionContextCloudEventExtension.class);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      throw new IllegalStateException("Could not find SDK implementation class or constructor!", e);
    }

    return new V1SdkLogic(sdkClassLoader, eventClassConstructor, contextClassConstructor);
  }

  @Override
  public List<SdkFunction> scan() {
    List<SdkFunction> results = new ArrayList<>();

    // Load interface classes from the project class loader
    final String functionApiFunctionInterfaceName =
        "com.salesforce.functions.jvm.sdk.SalesforceFunction";
    final String functionApiInvocationEventInterfaceName =
        "com.salesforce.functions.jvm.sdk.InvocationEvent";
    final String functionApiContextInterfaceName = "com.salesforce.functions.jvm.sdk.Context";

    final List<String> functionApiClassNames = new ArrayList<>();
    functionApiClassNames.add(functionApiFunctionInterfaceName);
    functionApiClassNames.add(functionApiInvocationEventInterfaceName);
    functionApiClassNames.add(functionApiContextInterfaceName);

    final Map<String, Class<?>> functionApiClasses = new HashMap<>();
    for (String functionApiClassName : functionApiClassNames) {
      try {
        Class<?> clazz = sdkClassLoader.loadClass(functionApiClassName);
        functionApiClasses.put(functionApiClassName, clazz);
      } catch (ClassNotFoundException e) {
        LOGGER.error(
            "Could not load function API class {}. Please ensure your project depends on the Java Functions API.",
            functionApiClassName,
            e);
        return Collections.emptyList();
      }
    }

    // Scan the project class loader for function implementations
    final ScanResult classpathScanResult =
        new ClassGraph()
            .overrideClassLoaders(sdkClassLoader)
            .enableClassInfo()
            .enableMethodInfo()
            .scan();

    final List<SalesforceFunction> foundFunctions = new ArrayList<>();

    for (ClassInfo classInfo :
        classpathScanResult.getClassesImplementing(functionApiFunctionInterfaceName)) {
      for (ClassRefTypeSignature typeSignature :
          classInfo.getTypeSignature().getSuperinterfaceSignatures()) {
        if (!typeSignature.getFullyQualifiedClassName().equals(functionApiFunctionInterfaceName)) {
          continue;
        }

        // Load function implementation class
        final Class<?> functionClass;
        try {
          functionClass = sdkClassLoader.loadClass(classInfo.getName());
        } catch (ClassNotFoundException e) {
          LOGGER.warn(
              "Could not find potential function's class ({}) in classpath.",
              classInfo.getName(),
              e);
          continue;
        }

        // Find apply method of function implementation class
        final Method functionApplyMethod;
        try {
          functionApplyMethod =
              functionClass.getMethod(
                  "apply",
                  functionApiClasses.get(functionApiInvocationEventInterfaceName),
                  functionApiClasses.get(functionApiContextInterfaceName));

          functionApplyMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
          LOGGER.warn(
              "Could not find apply(InvocationEvent<?>, Context) method in function class {}.",
              classInfo.getName(),
              e);
          continue;
        }

        // Create function instance
        final Object functionInstance;
        try {
          functionInstance = functionClass.getConstructor().newInstance();
        } catch (InstantiationException e) {
          LOGGER.warn("Could not instantiate function class {}.", classInfo.getName(), e);
          continue;
        } catch (IllegalAccessException e) {
          LOGGER.warn(
              "Could not instantiate function class {}. Is the constructor public?",
              classInfo.getName(),
              e);
          continue;
        } catch (InvocationTargetException e) {
          LOGGER.warn(
              "Exception while instantiating function class {}.",
              classInfo.getName(),
              e.getCause());
          continue;
        } catch (NoSuchMethodException e) {
          LOGGER.warn(
              "Could not find default constructor for function class {}.", classInfo.getName(), e);
          continue;
        }

        final TypeArgument payloadTypeArgument = typeSignature.getTypeArguments().get(0);
        final TypeArgument returnTypeArgument = typeSignature.getTypeArguments().get(1);

        results.add(
            new SdkFunction(
                functionClass.getName(),
                payloadTypeArgument,
                returnTypeArgument,
                new SdkV1SalesforceFunctionInvocable(
                    eventClassConstructor,
                    contextClassConstructor,
                    functionClass,
                    functionInstance,
                    functionApplyMethod)));
      }
    }

    return results;
  }
}
