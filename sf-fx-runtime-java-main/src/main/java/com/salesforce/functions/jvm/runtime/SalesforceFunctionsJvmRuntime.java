package com.salesforce.functions.jvm.runtime;

import com.salesforce.functions.jvm.runtime.project.Project;
import com.salesforce.functions.jvm.runtime.project.ProjectBuilder;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.project.ProjectFunctionsScanner;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SalesforceFunctionsJvmRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceFunctionsJvmRuntime.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            LOGGER.error("Usage: sf-fx-runtime-java $PROJECT_DIR");
            System.exit(-1);
        }

        Path projectPath = Paths.get(args[0]);

        LOGGER.info("Detecting project type at path {}...", projectPath);
        long projectDetectStart = System.currentTimeMillis();

        Optional<Project> optionalProject = Optional.empty();
        for (ProjectBuilder builder : findImplementationsOf(ProjectBuilder.class)) {
            optionalProject = builder.build(projectPath);

            if (optionalProject.isPresent()) {
                break;
            }
        }

        Project project = optionalProject
                .orElseThrow(() -> new IllegalStateException(String.format("Could not find project at path %s!", projectPath)));

        long projectDetectDuration = System.currentTimeMillis() - projectDetectStart;
        LOGGER.info("Detected {} project at path {} after {}ms!", project.getTypeName(), projectPath, projectDetectDuration);

        InvocationInterface invocationInterface = findImplementationsOf(InvocationInterface.class)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Could not find any invocation interfaces!"));

        LOGGER.info("Scanning project for functions...");
        long scanStart = System.currentTimeMillis();
        List<ProjectFunction> projectFunctions = ProjectFunctionsScanner.scan(project);
        long scanDuration = System.currentTimeMillis() - scanStart;

        projectFunctions.forEach(function -> {
            String parameters = function.getParameterTypes().stream().map(Class::toString).collect(Collectors.joining(", "));
            LOGGER.info("Found function: {} with {} parameters ({}) and return type {}", function.getName(), function.getArity(), parameters, function.getReturnType());
        });

        LOGGER.info("Found {} function(s) after {}ms.", projectFunctions.size(), scanDuration);

        // In cases of ambiguity, we want the user to be able to select which function to expose. For now, we just
        // select the first one we find.
        ProjectFunction projectFunction = projectFunctions.get(0);

        LOGGER.info("Starting invocation interface {} for function {}...", invocationInterface.getClass().getName(), projectFunction.getName());
        invocationInterface.start(projectFunction);
    }

    /*
     * Scans the classpath for implementations of the given interface and instantiates an instance of it with the
     * default constructor. Only really useful here and only for ProjectBuilders and InvocationInterfaces.
     */
    private static <A> List<A> findImplementationsOf(Class<A> interfaceClass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ArrayList<A> builders = new ArrayList<>();

        if (!interfaceClass.isInterface()) {
            return builders;
        }

        ScanResult scanResult = new ClassGraph().enableClassInfo().scan();
        for (ClassInfo classInfo : scanResult.getClassesImplementing(interfaceClass.getName())) {
            Class<?> projectBuilderClass = Class.forName(classInfo.getName());

            A builder = interfaceClass.cast(projectBuilderClass.getConstructor().newInstance());
            builders.add(builder);
        }

        return builders;
    }
}
