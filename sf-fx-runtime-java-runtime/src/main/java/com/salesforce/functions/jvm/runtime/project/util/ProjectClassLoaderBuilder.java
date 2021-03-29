/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.util;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProjectClassLoaderBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectClassLoaderBuilder.class);

  /**
   * Builds a custom, isolated, class loader for the given paths. This class loader will be a direct
   * child of the bootstrap classloader and has therefore only access to the JVM runtime itself and
   * the classes at the provided paths. In addition to directories of class files, paths to JAR
   * files are supported as well.
   *
   * @param paths The paths of classes and JAR files to make available with the new class loader.
   * @return The new class loader.
   */
  public static ClassLoader build(List<Path> paths) {
    // NOTE: It's important to keep the order of the given paths since URLClassLoader will load
    // classes in the
    // given order. See com.salesforce.functions.jvm.runtime.project.Project#createClassLoader for
    // details.
    URL[] urls = new URL[paths.size()];
    for (int i = 0; i < urls.length; i++) {
      urls[i] = pathToURLClassLoaderURL(paths.get(i));
    }

    // The Bootstrap class loader loads the basic runtime classes provided by the JVM, plus any
    // classes from JAR files present in the system extensions directory. It will not "see" the
    // classpath set for the JVM runtime. This creates a strong isolation of function runtime
    // classloading and the function provided by the user.
    ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();

    return AccessController.doPrivileged(
        (PrivilegedAction<ClassLoader>) () -> new URLClassLoader(urls, bootstrapClassLoader));
  }

  private static URL pathToURLClassLoaderURL(Path path) {
    String absolutePathAsString = path.toAbsolutePath().toString();

    // URLClassLoader strictly requires that directories end with a slash. Without it, the contents
    // of the directory will not be visible to the classloader.
    if (Files.isDirectory(path)) {
      absolutePathAsString += "/";
    }

    try {
      return new URI("file", null, absolutePathAsString, null, null).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      // This should never happen since we build our URL with the URI constructor that will escape
      // all parts of the URL for us.
      LOGGER.warn("Unexpected exception while preparing project class loader!", e);
      return null;
    }
  }

  private ProjectClassLoaderBuilder() {}
}
