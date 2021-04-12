/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.sdk.data.ReferenceId;
import java.util.Objects;

public class ReferenceIdImpl implements ReferenceId {
  private final String id;

  public ReferenceIdImpl(String id) {
    this.id = id;
  }

  public String toApiString() {
    return String.format("@{%s.id}", id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ReferenceIdImpl that = (ReferenceIdImpl) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
