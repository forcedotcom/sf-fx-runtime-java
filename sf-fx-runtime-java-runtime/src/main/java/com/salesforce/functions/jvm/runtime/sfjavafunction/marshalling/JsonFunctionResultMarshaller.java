/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.json.JsonLibrary;
import com.salesforce.functions.jvm.runtime.json.JsonLibraryDetector;
import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonSerializationException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionResultMarshallingException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class JsonFunctionResultMarshaller implements FunctionResultMarshaller {
  private final Type type;
  private final JsonLibrary jsonLibrary;

  public JsonFunctionResultMarshaller(Type type, ClassLoader classLoader)
      throws AmbiguousJsonLibraryException {
    this.type = type;
    this.jsonLibrary = JsonLibraryDetector.detect(type, classLoader);
  }

  public JsonFunctionResultMarshaller(Class<?> clazz) throws AmbiguousJsonLibraryException {
    this.type = clazz;
    this.jsonLibrary = JsonLibraryDetector.detect(clazz);
  }

  @Override
  public MediaType getMediaType() {
    return MediaType.JSON_UTF_8;
  }

  @Override
  public Type getSourceType() {
    return type;
  }

  @Override
  public byte[] marshallBytes(Object object) throws FunctionResultMarshallingException {
    try {
      return jsonLibrary.serialize(object).getBytes(StandardCharsets.UTF_8);
    } catch (JsonSerializationException e) {
      throw new FunctionResultMarshallingException("JSON serialization failed!", e);
    }
  }
}
