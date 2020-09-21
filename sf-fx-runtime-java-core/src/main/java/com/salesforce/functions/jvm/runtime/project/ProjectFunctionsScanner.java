package com.salesforce.functions.jvm.runtime.project;

import io.github.classgraph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ProjectFunctionsScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectFunctionsScanner.class);

    /**
     * Scans the given project for functions. It looks for java.util.function.Function and java.util.function.BiFunction
     * implementations with a default constructor. Other functions are not considered. Discovered functions will be
     * initialized and packaged as Function objects for later invocation.
     *
     * @see ProjectFunction
     * @param project The project to scan.
     * @return A list of all functions found in the given project.
     */
    public static List<ProjectFunction> scan(Project project) {
        Objects.requireNonNull(project);

        ClassLoader projectClassLoader = project.getClassLoader();

        ScanResult result = new ClassGraph()
                .overrideClassLoaders(projectClassLoader)
                .enableClassInfo()
                .enableMethodInfo()
                .scan();

        List<ProjectFunction> scanResult = new ArrayList<>();

        List<ClassInfo> classInfos = new ArrayList<>();
        classInfos.addAll(result.getClassesImplementing(FUNCTION_CLASS_NAME));
        classInfos.addAll(result.getClassesImplementing(BIFUNCTION_CLASS_NAME));

        for (ClassInfo classInfo : classInfos) {
            for (ClassRefTypeSignature typeSignature : classInfo.getTypeSignature().getSuperinterfaceSignatures()) {

                List<String> parameterTypeClassNames = new ArrayList<>();
                String returnTypeTypeArgumentClassName;

                List<TypeArgument> typeArguments = typeSignature.getTypeArguments();
                if (typeSignature.getFullyQualifiedClassName().equals(FUNCTION_CLASS_NAME)) {
                    parameterTypeClassNames.add(typeArguments.get(0).toString());
                    returnTypeTypeArgumentClassName = typeArguments.get(1).toString();
                } else if (typeSignature.getFullyQualifiedClassName().equals(BIFUNCTION_CLASS_NAME)) {
                    parameterTypeClassNames.add(typeArguments.get(0).toString());
                    parameterTypeClassNames.add(typeArguments.get(1).toString());
                    returnTypeTypeArgumentClassName = typeArguments.get(2).toString();
                } else {
                    continue;
                }

                try {
                    Class<?> functionType = projectClassLoader.loadClass(classInfo.getName());
                    Class<?> returnType = projectClassLoader.loadClass(returnTypeTypeArgumentClassName);

                    List<Class<?>> parameterTypes = new ArrayList<>();
                    for (String parameterTypeClassName : parameterTypeClassNames) {
                        parameterTypes.add(projectClassLoader.loadClass(parameterTypeClassName));
                    }

                    Object functionInstance;
                    try {
                        functionInstance = functionType.getConstructor().newInstance();
                    } catch (NoSuchMethodException e) {
                        LOGGER.warn("Potential function {} does not have a default constructor and cannot be used!", functionType.getName());
                        continue;
                    }

                    Method functionMethod = functionType.getMethod("apply", parameterTypes.toArray(new Class[0]));

                    ProjectFunction projectFunction = new ProjectFunction(project, functionInstance, functionMethod, parameterTypes, returnType);
                    scanResult.add(projectFunction);

                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
                    // These exceptions should not occur as long as the signatures of Function and BiFunction do not
                    // change.
                    LOGGER.error("Unexpected error while scanning. Scanning will continue.", e);
                }
            }
        }

        return Collections.unmodifiableList(scanResult);
    }

    private ProjectFunctionsScanner() {
    }

    private static final String FUNCTION_CLASS_NAME = "java.util.function.Function";
    private static final String BIFUNCTION_CLASS_NAME = "java.util.function.BiFunction";
}
