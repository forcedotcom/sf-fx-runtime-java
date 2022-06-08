/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import com.salesforce.functions.jvm.sdk.bulk.*;

public final class JobInfoBuilder {
  private String id;
  private String objectType;
  private Operation operation;
  private JobState jobState;

  private JobInfoBuilder() {}

  public static JobInfoBuilder createJobInfoBuilder() {
    return new JobInfoBuilder();
  }

  public JobInfoBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public JobInfoBuilder withObjectType(String objectType) {
    this.objectType = objectType;
    return this;
  }

  public JobInfoBuilder withOperation(Operation operation) {
    this.operation = operation;
    return this;
  }

  public JobInfoBuilder withJobState(JobState jobState) {
    this.jobState = jobState;
    return this;
  }

  public JobInfo build() {
    return new JobInfo(id, objectType, operation, jobState);
  }
}
