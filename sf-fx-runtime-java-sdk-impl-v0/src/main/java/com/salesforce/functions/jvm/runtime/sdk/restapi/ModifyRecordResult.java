/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

public class ModifyRecordResult {
  private final String id;

  public ModifyRecordResult(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
