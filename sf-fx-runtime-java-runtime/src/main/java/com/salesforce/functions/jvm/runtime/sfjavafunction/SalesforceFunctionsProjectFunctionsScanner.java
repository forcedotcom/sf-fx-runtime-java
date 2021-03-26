/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sfjavafunction;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectFunctionsScanner;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.SalesforceFunctionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.*;
import io.cloudevents.CloudEvent;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/**
 * ProjectFunctionsScanner implementation that scans the classpath for user functions that comply to the Salesforce
 * Functions Java API. It will return functions wrapped in a {@link SalesforceFunction} that accepts a
 * {@link CloudEvent} as the sole input argument and transforms it accordingly.
 * <p>
 * This functions scanner will log issues with functions that look like they should be compatible but aren't for
 * whatever reason. Customers can use these error messages to fix mistakes in their function definition.
 */
public class SalesforceFunctionsProjectFunctionsScanner implements ProjectFunctionsScanner<SalesforceFunction, CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceFunctionsProjectFunctionsScanner.class);

    @Override
    public List<SalesforceFunction> scan(Project project) {
        Objects.requireNonNull(project);

        // We inject a slf4j compatible logger implementation into the project classloader in the next step. To do so,
        // we need to get the implementation JAR out of the invoker JAR:
        Path loggerJarPath;
        try {
            Optional<Path> optionalLoggerJarPath = copyJarFileFromClassPath("sf-fx-java-logger.jar");
            if (!optionalLoggerJarPath.isPresent()) {
                LOGGER.error("Could not find logger implementation JAR!");
                return Collections.emptyList();
            }

            loggerJarPath = optionalLoggerJarPath.get();
        } catch (IOException e) {
            LOGGER.error("Could not copy logger JAR file to disk!", e);
            return Collections.emptyList();
        }

        // Class loader for the user project, does only contain classes (including dependencies) from the the
        // user-defined project and bootstrap classes. Specifically no SDK implementation! This ensures no classes leak
        // from the runtime to the function. The only exception is the injected logger JAR file since it needs to be
        // visible for the classes inside the project.
        final ClassLoader projectClassLoader = project.createClassLoader(loggerJarPath);

        // Class loader that exposes a subset of classes from the general runtime class loader. This is necessary to
        // share some classes between the SDK class loader (see below) and the runtime.
        //
        // The SDK needs to be initialized with a CloudEvent and the required Salesforce extensions. To be able to pass
        // those objects to the SDK constructor/initializer, both the runtime and the SDK class loader need to use the
        // same class loaded from the same class loader.
        final AllowListClassLoader allowListClassLoader = new AllowListClassLoader(
                // Use the classloader from this class which should be the regular classloader for the runtime.
                this.getClass().getClassLoader(),
                // Parent class loader
                projectClassLoader,
                "io.cloudevents.",
                "com.salesforce.functions.jvm.runtime.cloudevent."
        );

        // Class loader that can load SDK implementation classes. It will fall back to the AllowListClassLoader above
        // to load CloudEvent related classes. The class loader above that one is the project class loader that contains
        // the SDK interface required by the SDK implementation. It's important that the SDK implementation does not
        // contain the CloudEvent classes nor the SDK interface. This can be achieved by using the "provided" scope in
        // Maven for the SDK implementation project.
        final ClassLoader sdkClassLoader;

        // In the future when we might have multiple incompatible SDK interfaces, we can implement different strategies
        // for each SDK version initialization and function detection. This is not a concern right now and therefore
        // unimplemented. The SDK does include a properties files that can be used to reliably detect the SDK version:
        //
        // final Properties properties = new Properties();
        // try (final InputStream stream = projectClassLoader.getResourceAsStream("sf-fx-sdk-java.properties")) {
        //   properties.load(stream);
        // }
        //
        // properties.getProperty("version");

        try {
            Optional<Path> optionalSdkImplementationJarPath = copyJarFileFromClassPath("sdk-impl-v0.jar");
            if (!optionalSdkImplementationJarPath.isPresent()) {
                LOGGER.error("Could not find logger implementation JAR!");
                return Collections.emptyList();
            }

            sdkClassLoader
                    = new URLClassLoader(new URL[]{optionalSdkImplementationJarPath.get().toUri().toURL()}, allowListClassLoader);

        } catch (MalformedURLException e) {
            LOGGER.error("Unexpected exception while preparing SDK implementation classloader!", e);
            return Collections.emptyList();
        } catch (IOException e) {
            LOGGER.error("Could copy SDK implementation JAR to disk!", e);
            return Collections.emptyList();
        }

        final Constructor<?> eventClassConstructor;
        final Constructor<?> contextClassConstructor;
        try {
            Class<?> eventClass = sdkClassLoader.loadClass("com.salesforce.functions.jvm.runtime.sdk.InvocationEvent");
            eventClassConstructor = eventClass.getConstructor(CloudEvent.class, Object.class);

            Class<?> contextClass = sdkClassLoader.loadClass("com.salesforce.functions.jvm.runtime.sdk.Context");
            contextClassConstructor = contextClass.getConstructor(CloudEvent.class, SalesforceContextCloudEventExtension.class, SalesforceFunctionContextCloudEventExtension.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LOGGER.error("Could not find SDK implementation class or constructor!", e);
            return Collections.emptyList();
        }

        // Load interface classes from the project class loader
        final String functionApiFunctionInterfaceName = "com.salesforce.functions.jvm.sdk.SalesforceFunction";
        final String functionApiInvocationEventInterfaceName = "com.salesforce.functions.jvm.sdk.InvocationEvent";
        final String functionApiContextInterfaceName = "com.salesforce.functions.jvm.sdk.Context";

        final List<String> functionApiClassNames = new ArrayList<>();
        functionApiClassNames.add(functionApiFunctionInterfaceName);
        functionApiClassNames.add(functionApiInvocationEventInterfaceName);
        functionApiClassNames.add(functionApiContextInterfaceName);

        final Map<String, Class<?>> functionApiClasses = new HashMap<>();
        for (String functionApiClassName : functionApiClassNames) {
            try {
                Class<?> clazz = projectClassLoader.loadClass(functionApiClassName);
                functionApiClasses.put(functionApiClassName, clazz);
            } catch (ClassNotFoundException e) {
                LOGGER.error("Could not load function API class {}. Please ensure your project depends on the Java Functions API.", functionApiClassName, e);
                return Collections.emptyList();
            }
        }

        // Scan the project class loader for function implementations
        final ScanResult classpathScanResult = new ClassGraph()
                .overrideClassLoaders(projectClassLoader)
                .enableClassInfo()
                .enableMethodInfo()
                .scan();

        final List<SalesforceFunction> foundFunctions = new ArrayList<>();

        for (ClassInfo classInfo : classpathScanResult.getClassesImplementing(functionApiFunctionInterfaceName)) {
            for (ClassRefTypeSignature typeSignature : classInfo.getTypeSignature().getSuperinterfaceSignatures()) {
                if (!typeSignature.getFullyQualifiedClassName().equals(functionApiFunctionInterfaceName)) {
                    continue;
                }

                // Load function implementation class
                final Class<?> functionClass;
                try {
                    functionClass = projectClassLoader.loadClass(classInfo.getName());
                } catch (ClassNotFoundException e) {
                    LOGGER.warn("Could not find potential function's class ({}) in classpath.", classInfo.getName(), e);
                    continue;
                }

                // Find apply method of function implementation class
                final Method functionApplyMethod;
                try {
                    functionApplyMethod = functionClass.getMethod(
                            "apply",
                            functionApiClasses.get(functionApiInvocationEventInterfaceName),
                            functionApiClasses.get(functionApiContextInterfaceName)
                    );

                    functionApplyMethod.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    LOGGER.warn("Could not find apply(InvocationEvent<?>, Context) method in function class {}.", classInfo.getName(), e);
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
                    LOGGER.warn("Could not instantiate function class {}. Is the constructor public?", classInfo.getName(), e);
                    continue;
                } catch (InvocationTargetException e) {
                    LOGGER.warn("Exception while instantiating function class {}.", classInfo.getName(), e.getCause());
                    continue;
                } catch (NoSuchMethodException e) {
                    LOGGER.warn("Could not find default constructor for function class {}.", classInfo.getName(), e);
                    continue;
                }

                final String payloadTypeArgument = typeSignature.getTypeArguments().get(0).toString();
                final String returnTypeString = typeSignature.getTypeArguments().get(1).toString();

                // Determine PayloadUnmarshaller for this user function
                PayloadUnmarshaller unmarshaller = null;
                if (payloadTypeArgument.equals("byte[]")) {
                    unmarshaller = new ByteArrayPayloadUnmarshaller();
                } else {
                    try {
                        Class<?> clazz = projectClassLoader.loadClass(payloadTypeArgument);
                        unmarshaller = new PojoFromJsonPayloadUnmarshaller(clazz);
                    } catch (ClassNotFoundException e) {
                        // Intentional ignore
                    } catch (AmbiguousJsonLibraryException e) {
                        LOGGER.warn("Potential function {} declares an payload type with multiple JSON framework annotations. Function will be ignored!", functionClass.getName());
                    }
                }

                if (unmarshaller == null) {
                    LOGGER.warn("Potential function {} declares an unsupported payload type: {}. Function will be ignored!", functionClass.getName(), payloadTypeArgument);
                    continue;
                }

                // Determine FunctionResultMarshaller for this user function
                FunctionResultMarshaller marshaller = null;
                if (returnTypeString.equals("byte[]")) {
                    marshaller = new ByteArrayFunctionResultMarshaller();
                } else if (returnTypeString.equals("java.lang.String")) {
                    marshaller = new StringFunctionResultMarshaller();
                } else {
                    try {
                        Class<?> clazz = projectClassLoader.loadClass(returnTypeString);
                        marshaller = new PojoAsJsonFunctionResultMarshaller(clazz);
                    } catch (ClassNotFoundException e) {
                        // Intentional ignore
                    } catch (AmbiguousJsonLibraryException e) {
                        LOGGER.warn("Potential function {} declares a return type with multiple JSON framework annotations. Function will be ignored!", functionClass.getName());
                    }
                }

                if (marshaller == null) {
                    LOGGER.warn("Potential function {} declares an unsupported return type: {}! Function will be ignored!", functionClass.getName(), returnTypeString);
                    continue;
                }

                Method mdcClearMethod = null;
                Method mdcPutMethod = null;
                try {
                    // We look for slf4j in the topmost class loader to make sure if any class in the tree wants to log,
                    // it can, even when the user project does not declare a dependency on slf4j.
                    Class<?> mdcClass = sdkClassLoader.loadClass("org.slf4j.MDC");
                    mdcClearMethod = mdcClass.getMethod("clear");
                    mdcPutMethod = mdcClass.getMethod("put", String.class, String.class);
                } catch (ClassNotFoundException e) {
                    // It's fine to not have slf4j on the classpath since that indicates that no logging is taking place
                    // in customers or SDK code anyway.
                    LOGGER.debug("Could not find org.slf4j.MDC in classpath, invocation context data will not be available in logger.");
                } catch (NoSuchMethodException e) {
                    LOGGER.warn("Could not find required method on org.slf4j.MDC. Invocation context data will not be available in logger.");
                }

                final Method finalMdcClearMethod = mdcClearMethod;
                final Method finalMdcPutMethod = mdcPutMethod;
                SalesforceFunction salesforceFunction = new SalesforceFunction(
                        unmarshaller,
                        marshaller,
                        (payload, cloudEvent, salesforceContext, functionContext) -> {
                            Object event = eventClassConstructor.newInstance(cloudEvent, payload);
                            Object context = contextClassConstructor.newInstance(cloudEvent, salesforceContext, functionContext);

                            if (finalMdcClearMethod != null) {
                                finalMdcClearMethod.invoke(null);
                            }

                            if (finalMdcPutMethod != null) {
                                finalMdcPutMethod.invoke(null, "function-invocation-id", cloudEvent.getId());
                            }

                            return functionApplyMethod.invoke(functionInstance, event, context);
                        }
                );

                foundFunctions.add(salesforceFunction);
            }
        }

        return foundFunctions;
    }

    /**
     * Copies a JAR file from the current class loader to disk.
     *
     * This exists since URLClassLoader does not work well with "jar:" URLs. As a workaround, we copy the JAR file from
     * the JAR file to a temporary location and load the classes from there.
     *
     * @param name The name of the JAR file to copy.
     * @return An Optional containing the path to the temporary file or an undefined Optional if the file could not be
     * found in the current class loader.
     * @throws IOException If an IO related error occured.
     */
    private Optional<Path> copyJarFileFromClassPath(String name) throws IOException {
        InputStream inputStream
                = getClass().getClassLoader().getResourceAsStream(name);

        if (inputStream == null) {
            return Optional.empty();
        }

        Path jarFilePath = Files.createTempFile(name, ".tmp.jar");
        Files.copy(inputStream, jarFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return Optional.of(jarFilePath);
    }
}
