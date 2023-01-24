/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.ModifyRecordResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.json.JsonRestApiRequest;
import com.salesforce.functions.jvm.sdk.data.UnitOfWork;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UnitOfWorkImpl implements UnitOfWork {
  // Order is important, don't replace LinkedHashMap without verifying the new implementation
  // also preserves insertion order!
  private final LinkedHashMap<String, JsonRestApiRequest<ModifyRecordResult>> subrequests;

  public UnitOfWorkImpl(LinkedHashMap<String, JsonRestApiRequest<ModifyRecordResult>> subrequests) {
    this.subrequests = subrequests;
  }

  public Map<String, JsonRestApiRequest<ModifyRecordResult>> getSubrequests() {
    return Collections.unmodifiableMap(subrequests);
  }
}
