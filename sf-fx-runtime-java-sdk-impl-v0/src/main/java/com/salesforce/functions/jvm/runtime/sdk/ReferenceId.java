/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import java.util.Objects;

public class ReferenceId implements com.salesforce.functions.jvm.sdk.data.ReferenceId {
  private final String id;

  public ReferenceId(String id) {
    this.id = id;
  }

  @Override
  public String toApiString() {
    return String.format("@{%s.id}", id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ReferenceId that = (ReferenceId) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
