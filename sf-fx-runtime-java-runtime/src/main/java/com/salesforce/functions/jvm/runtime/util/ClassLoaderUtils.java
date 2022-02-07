/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ClassLoaderUtils {

  /**
   * Copies a file from a ClassLoader to disk.
   *
   * <p>This exists since URLClassLoader does not work well with "jar:" URLs. As a workaround, we
   * copy the JAR file from the JAR file to a temporary location and load the classes from there.
   *
   * @param name The name of the file to copy.
   * @return An Optional containing the path to the temporary file or an undefined Optional if the
   *     file could not be found in the current class loader.
   * @throws IOException If an IO related error occurred.
   */
  public static Optional<Path> copyFileFromClassLoader(ClassLoader classLoader, String name)
      throws IOException {
    InputStream inputStream = classLoader.getResourceAsStream(name);

    if (inputStream == null) {
      return Optional.empty();
    }

    Path jarFilePath = Files.createTempFile(name, ".tmp");
    Files.copy(inputStream, jarFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

    return Optional.of(jarFilePath);
  }

  private ClassLoaderUtils() {}
}
