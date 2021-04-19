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
import com.salesforce.functions.jvm.sdk.data.RecordCreate;
import com.salesforce.functions.jvm.sdk.data.RecordUpdate;
import com.salesforce.functions.jvm.sdk.data.ReferenceId;
import com.salesforce.functions.jvm.sdk.data.UnitOfWork;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class UnitOfWorkImpl implements UnitOfWork {
  // Order is important, don't replace LinkedHashMap without verifying the new implementation
  // also preserves insertion order!
  private final Map<String, RestApiRequest<ModifyRecordResult>> subrequests = new LinkedHashMap<>();
  private final AtomicInteger nextReferenceId = new AtomicInteger(0);

  @Override
  @Nonnull
  public ReferenceId registerCreate(RecordCreate create) {
    RecordCreateImpl impl = (RecordCreateImpl) create;

    String referenceId = nextReferenceId();
    subrequests.put(referenceId, new CreateRecordRestApiRequest(impl.getType(), impl.getValues()));

    return new ReferenceIdImpl(referenceId);
  }

  @Override
  @Nonnull
  public ReferenceId registerUpdate(RecordUpdate update) {
    RecordUpdateImpl impl = (RecordUpdateImpl) update;

    String referenceId = nextReferenceId();
    subrequests.put(
        referenceId,
        new UpdateRecordRestApiRequest(impl.getId(), impl.getType(), impl.getValues()));

    return new ReferenceIdImpl(referenceId);
  }

  public Map<String, RestApiRequest<ModifyRecordResult>> getSubrequests() {
    return Collections.unmodifiableMap(subrequests);
  }

  private String nextReferenceId() {
    return "referenceId" + nextReferenceId.getAndIncrement();
  }
}
