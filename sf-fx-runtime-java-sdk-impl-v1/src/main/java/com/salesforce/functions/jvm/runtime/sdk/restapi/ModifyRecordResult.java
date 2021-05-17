/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import java.util.Objects;
import javax.annotation.Nonnull;

public final class ModifyRecordResult {
  private final String id;

  public ModifyRecordResult(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "ModifyRecordResult{" + "id='" + id + '\'' + '}';
  }

  @Nonnull
  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModifyRecordResult that = (ModifyRecordResult) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
