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
}
