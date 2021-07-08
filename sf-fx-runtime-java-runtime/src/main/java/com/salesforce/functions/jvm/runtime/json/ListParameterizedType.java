/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Used to determine the "type" of a dynamic class at runtime for JSON deserializing via the Gson
 * libray
 *
 * <p>``` Type type = new ListParameterizedType(clazz); return new Gson().fromJson(new
 * String(data.toBytes(), StandardCharsets.UTF_8), type); ```
 *
 * <p>From https://stackoverflow.com/a/25223817/147390
 */
public class ListParameterizedType implements ParameterizedType {

  private final Type type;

  public ListParameterizedType(Type type) {
    this.type = type;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return new Type[] {type};
  }

  @Override
  public Type getRawType() {
    return List.class;
  }

  @Override
  public Type getOwnerType() {
    return null;
  }

  // implement equals method too! (as per javadoc)
}
