/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.DeleteRecordRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.ModifyRecordResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiRequest;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.ReferenceId;
import com.salesforce.functions.jvm.sdk.data.UnitOfWork;
import com.salesforce.functions.jvm.sdk.data.builder.UnitOfWorkBuilder;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class UnitOfWorkBuilderImpl implements UnitOfWorkBuilder {
  // Order is important, don't replace LinkedHashMap without verifying the new implementation
  // also preserves insertion order!
  private final LinkedHashMap<String, RestApiRequest<ModifyRecordResult>> subrequests =
      new LinkedHashMap<>();

  private final AtomicInteger nextReferenceId = new AtomicInteger(0);

  @Nonnull
  @Override
  public ReferenceId registerCreate(Record record) {
    String referenceId = nextReferenceId();
    subrequests.put(referenceId, DataApiImpl.apiRequestForCreate(record));
    return new ReferenceIdImpl(referenceId);
  }

  @Nonnull
  @Override
  public ReferenceId registerUpdate(Record record) {
    String referenceId = nextReferenceId();
    subrequests.put(referenceId, DataApiImpl.apiRequestForUpdate(record));
    return new ReferenceIdImpl(referenceId);
  }

  @Nonnull
  @Override
  public ReferenceId registerDelete(String type, String id) {
    String referenceId = nextReferenceId();
    subrequests.put(referenceId, new DeleteRecordRestApiRequest(type, id));
    return new ReferenceIdImpl(referenceId);
  }

  @Nonnull
  @Override
  public UnitOfWork build() {
    return new UnitOfWorkImpl(subrequests);
  }

  private String nextReferenceId() {
    return "referenceId" + nextReferenceId.getAndIncrement();
  }
}
