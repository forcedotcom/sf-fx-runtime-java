/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.CreateRecordRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.ModifyRecordResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.UpdateRecordRestApiRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

public class UnitOfWork implements com.salesforce.functions.jvm.sdk.data.UnitOfWork {
  // Order is important, don't replace LinkedHashMap without verifying the new implementation
  // also preserves insertion order!
  private final Map<String, RestApiRequest<ModifyRecordResult>> subrequests = new LinkedHashMap<>();

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.data.ReferenceId registerCreate(
      com.salesforce.functions.jvm.sdk.data.RecordCreate create) {
    RecordCreate impl = (RecordCreate) create;

    String referenceId = generateReferenceId();
    subrequests.put(referenceId, new CreateRecordRestApiRequest(impl.getType(), impl.getValues()));

    return new ReferenceId(referenceId);
  }

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.data.ReferenceId registerUpdate(
      com.salesforce.functions.jvm.sdk.data.RecordUpdate update) {
    RecordUpdate impl = (RecordUpdate) update;

    String referenceId = generateReferenceId();
    subrequests.put(
        referenceId,
        new UpdateRecordRestApiRequest(impl.getType(), impl.getId(), impl.getValues()));

    return new ReferenceId(referenceId);
  }

  public Map<String, RestApiRequest<ModifyRecordResult>> getSubrequests() {
    return Collections.unmodifiableMap(subrequests);
  }

  private String generateReferenceId() {
    return UUID.randomUUID().toString().replace('-', 'x');
  }
}
