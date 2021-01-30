package com.salesforce.functions.jvm.runtime.sfjavafunction;

import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

        List<SalesforceFunction> foundFunctions = new ArrayList<>();

        ClassLoader projectClassLoader = project.getClassLoader();

        List<String> functionApiClassNames = new ArrayList<>();
        functionApiClassNames.add(FUNCTION_API_FUNCTION_CLASS_NAME);
        functionApiClassNames.add(FUNCTION_API_EVENT_CLASS_NAME);
        functionApiClassNames.add(FUNCTION_API_CONTEXT_CLASS_NAME);
        functionApiClassNames.add(FUNCTION_API_ORG_CONTEXT_CLASS_NAME);
        functionApiClassNames.add(FUNCTION_API_USER_CONTEXT_CLASS_NAME);

        Map<String, Class<?>> functionApiClasses = new HashMap<>();
        for (String functionApiClassName : functionApiClassNames) {
            try {
                Class<?> clazz = projectClassLoader.loadClass(functionApiClassName);
                functionApiClasses.put(functionApiClassName, clazz);
            } catch (ClassNotFoundException e) {
                LOGGER.error("Could not load function API class {}. Please ensure your project depends on the Java Functions API.", functionApiClassName, e);
                return Collections.emptyList();
            }
        }

        ScanResult classpathScanResult = new ClassGraph()
                .overrideClassLoaders(projectClassLoader)
                .enableClassInfo()
                .enableMethodInfo()
                .scan();

        for (ClassInfo classInfo : classpathScanResult.getClassesImplementing(FUNCTION_API_FUNCTION_CLASS_NAME)) {
            for (ClassRefTypeSignature typeSignature : classInfo.getTypeSignature().getSuperinterfaceSignatures()) {
                if (!typeSignature.getFullyQualifiedClassName().equals(FUNCTION_API_FUNCTION_CLASS_NAME)) {
                    continue;
                }

                Class<?> functionClass = null;
                try {
                    functionClass = projectClassLoader.loadClass(classInfo.getName());
                } catch (ClassNotFoundException e) {
                    LOGGER.warn("Could not find potential function's class ({}) in classpath.", classInfo.getName(), e);
                    continue;
                }

                Method functionApplyMethod = null;
                try {
                    functionApplyMethod = functionClass.getMethod(
                            "apply",
                            functionApiClasses.get(FUNCTION_API_EVENT_CLASS_NAME),
                            functionApiClasses.get(FUNCTION_API_CONTEXT_CLASS_NAME)
                    );

                    functionApplyMethod.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    LOGGER.warn("Could not find apply(Event<?>, Context) method in function class {}.", classInfo.getName(), e);
                    continue;
                }

                Object functionInstance = null;
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

                String payloadTypeArgument = typeSignature.getTypeArguments().get(0).toString();
                String returnTypeString = typeSignature.getTypeArguments().get(1).toString();

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

                SalesforceFunction salesforceFunction = new SalesforceFunction(
                        unmarshaller,
                        marshaller,
                        projectClassLoader,
                        functionInstance,
                        functionApplyMethod,
                        functionApiClasses.get(FUNCTION_API_EVENT_CLASS_NAME),
                        functionApiClasses.get(FUNCTION_API_CONTEXT_CLASS_NAME),
                        functionApiClasses.get(FUNCTION_API_ORG_CONTEXT_CLASS_NAME),
                        functionApiClasses.get(FUNCTION_API_USER_CONTEXT_CLASS_NAME)
                );

                foundFunctions.add(salesforceFunction);
            }
        }

        return foundFunctions;
    }

    private final String FUNCTION_API_FUNCTION_CLASS_NAME = "com.salesforce.functions.jvm.sdk.SalesforceFunction";
    private final String FUNCTION_API_EVENT_CLASS_NAME = "com.salesforce.functions.jvm.sdk.Event";
    private final String FUNCTION_API_CONTEXT_CLASS_NAME = "com.salesforce.functions.jvm.sdk.Context";
    private final String FUNCTION_API_ORG_CONTEXT_CLASS_NAME = "com.salesforce.functions.jvm.sdk.OrgContext";
    private final String FUNCTION_API_USER_CONTEXT_CLASS_NAME = "com.salesforce.functions.jvm.sdk.UserContext";
}
