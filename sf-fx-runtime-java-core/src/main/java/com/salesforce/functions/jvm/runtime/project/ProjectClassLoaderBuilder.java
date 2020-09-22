package com.salesforce.functions.jvm.runtime.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.List;

public final class ProjectClassLoaderBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectClassLoaderBuilder.class);

    /**
     * Builds a custom, isolated, class loader for the given paths. This class loader will be a direct child of the
     * bootstrap classloader and has therefore only access to the JVM runtime itself and the classes at the provided
     * paths. In addition to directories of class files, paths to JAR files are supported as well.
     *
     * @param paths The paths of classes and JAR files to make available with the new class loader.
     * @return The new class loader.
     */
    public static ClassLoader build(List<Path> paths) {
        URL[] urls = new URL[paths.size()];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = pathToURLClassLoaderURL(paths.get(i));
        }

        // The Bootstrap class loader loads the basic runtime classes provided by the JVM, plus any classes from JAR
        // files present in the system extensions directory. It will not "see" the classpath set for the JVM runtime.
        // This creates a strong isolation of function runtime classloading and the function provided by the user.
        ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();

        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
            return new URLClassLoader(urls, bootstrapClassLoader);
        });
    }

    private static URL pathToURLClassLoaderURL(Path path) {
        String absolutePathAsString = path.toAbsolutePath().toString();

        // URLClassLoader strictly requires that directories end with a slash. Without it, the contents of the
        // directory will not be visible to the classloader.
        if (Files.isDirectory(path)) {
            absolutePathAsString += "/";
        }

        try {
            return new URI("file", null, absolutePathAsString, null, null).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            // This should never happen since we build our URL with the URI constructor that will escape all parts
            // of the URL for us.
            LOGGER.warn("Unexpected exception while preparing project class loader!", e);
            return null;
        }
    }

    private ProjectClassLoaderBuilder() {}
}
