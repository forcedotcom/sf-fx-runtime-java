/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonLibraryNotPresentException;
import java.util.ArrayList;
import java.util.List;

public final class JsonLibraryDetector {

  /**
   * Detects which JsonLibrary should be used with the given class. If no specific JsonLibrary is
   * strictly required for the given class, a fallback JsonLibrary will be used as a catch-all.
   *
   * @see JsonLibrary#mustBeUsedFor(java.lang.reflect.Type)
   * @param clazz The class to detect the JSON library for.
   * @return The JsonLibrary to use with the given class.
   * @throws AmbiguousJsonLibraryException If multiple JSON libraries announce they must be used for
   *     the given class.
   */
  public static JsonLibrary detect(Class<?> clazz) throws AmbiguousJsonLibraryException {
    ClassLoader classLoader = clazz.getClassLoader();

    // getClassLoader returns null when the class was loaded by the bootstrap classloader. To avoid
    // juggling with null pointers downstream, we normalize the variable here by putting the actual
    // bootstrap classloader in.
    if (classLoader == null) {
      classLoader = ClassLoader.getSystemClassLoader().getParent();
    }

    List<JsonLibrary> availableJsonLibraries = new ArrayList<>();

    try {
      availableJsonLibraries.add(new GsonReflectionJsonLibrary(classLoader));
    } catch (JsonLibraryNotPresentException e) {
      // If the library is not present in the user project's classpath, it cannot be used in any
      // case. We can safely ignore this exception and carry on.
    }

    try {
      availableJsonLibraries.add(new JacksonReflectionJsonLibrary(classLoader));
    } catch (JsonLibraryNotPresentException e) {
      // If the library is not present in the user project's classpath, it cannot be used in any
      // case. We can safely ignore this exception and carry on.
    }

    // Find the JSON library that declares to be responsible for the given class
    JsonLibrary responsibleJsonLibrary = null;
    for (JsonLibrary jsonLibrary : availableJsonLibraries) {
      if (jsonLibrary.mustBeUsedFor(clazz)) {
        if (responsibleJsonLibrary != null) {
          throw new AmbiguousJsonLibraryException(
              String.format(
                  "Multiple JSON libraries declared responsibility for class %s!",
                  clazz.getName()));
        }

        responsibleJsonLibrary = jsonLibrary;
      }
    }

    /*
    If no library declared to be responsible for the given class, we will use a Gson instance from the runtime
    itself. It will work fine with classes from foreign class loaders, but annotation detection won't since
    they're technically a different class than the ones from the runtime Gson instance.

    This shouldn't be much of a problem since the Gson reflection support should've declared responsibility when
    the class has Gson annotations and we never reaches this code.
    */
    if (responsibleJsonLibrary == null) {
      responsibleJsonLibrary = new GsonJsonLibrary();
    }

    return responsibleJsonLibrary;
  }

  private JsonLibraryDetector() {}
}
