/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonSerializationException;
import java.lang.reflect.Type;

/**
 * An abstracted interface to a JSON library.
 *
 * <p>This is used to provided access to user-supplied JSON libraries (i.e. for their Project's
 * class loader via reflection or runtime generated bytecode) as well as runtime native JSON
 * libraries under one abstract interface. Since the common denominator of all those libraries and
 * means to invoke them is very small, the interface is very specific to the needs of the Salesforce
 * Functions JVM runtime.
 */
public interface JsonLibrary {
  /**
   * Deserializes the given JSON string to an Object of the given class.
   *
   * @param json The JSON string to deserialize.
   * @param type The type of the object the JSON string should be deserialized to.
   * @param path The path where to find the object in the given JSON string. Useful when the target
   *     JSON object is wrapped in one or more objects. Can be empty.
   * @return The given JSON object as a Java object of the given class. The exact outcome of this
   *     method is library specific.
   * @throws JsonDeserializationException When the deserialization failed for any reason. Look at
   *     the exception's cause for the underlying library exception.
   */
  Object deserializeAt(String json, Type type, String... path) throws JsonDeserializationException;

  /**
   * Serializes the given object to a JSON compliant string. The exact outcome of this method is
   * library specific (i.e. based on annotations) and therefore no concrete promises about the shape
   * of the result can be made.
   *
   * @param object The object to serialize.
   * @return The serialized object as a JSON string.
   * @throws JsonSerializationException When the serialization failed for any reason. Look at the
   *     exception's cause for the underlying library exception.
   */
  String serialize(Object object) throws JsonSerializationException;

  /**
   * Returns if the given type must be processed by this JSON library. Implementations should only
   * return true when the type strongly indicates it should be processed with this library. This is
   * usually decided when the type has library specific annotations but different mechanisms can be
   * used too. Implementations must not return true just to indicate that a class can be processed
   * by this library.
   *
   * @param clazz The type to check.
   * @return Whether the JSON library must be used for this type.
   */
  boolean mustBeUsedFor(Type clazz);
}
