/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.sdk.bulk.*;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class JobInfo {
  private final String id;

  @SerializedName("object")
  private final String objectType;

  private final Operation operation;

  @SerializedName("state")
  private final JobState jobState;

  JobInfo(String id, String objectType, Operation operation, JobState jobState) {
    this.id = id;
    this.objectType = objectType;
    this.operation = operation;
    this.jobState = jobState;
  }

  @Nonnull
  public String getId() {
    return id;
  }

  @Nonnull
  public String getObjectType() {
    return objectType;
  }

  public Operation getOperation() {
    return operation;
  }

  public JobState getJobState() {
    return jobState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JobInfo jobInfo = (JobInfo) o;
    return Objects.equals(getId(), jobInfo.getId())
        && Objects.equals(getObjectType(), jobInfo.getObjectType())
        && getOperation() == jobInfo.getOperation()
        && getJobState() == jobInfo.getJobState();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getObjectType(), getOperation(), getJobState());
  }

  @Override
  public String toString() {
    return "JobInfo{"
        + "id='"
        + id
        + '\''
        + ", objectType='"
        + objectType
        + '\''
        + ", operation="
        + operation
        + ", jobState="
        + jobState
        + '}';
  }
}
