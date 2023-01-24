/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk;

import com.google.gson.annotations.SerializedName;

public enum BulkJobState {
  @SerializedName("Open")
  OPEN,
  @SerializedName("UploadComplete")
  UPLOAD_COMPLETE,
  @SerializedName("InProgress")
  IN_PROGRESS,
  @SerializedName("Aborted")
  ABORTED,
  @SerializedName("JobComplete")
  JOB_COMPLETE,
  @SerializedName("Failed")
  FAILED
}
