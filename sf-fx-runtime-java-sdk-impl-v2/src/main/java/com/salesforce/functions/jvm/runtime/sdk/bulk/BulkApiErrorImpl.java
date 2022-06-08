/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiError;
import com.salesforce.functions.jvm.sdk.bulk.error.BulkApiError;
import java.util.List;
import javax.annotation.Nonnull;

public class BulkApiErrorImpl implements BulkApiError {
  private final RestApiError apiError;

  public BulkApiErrorImpl(RestApiError apiError) {
    this.apiError = apiError;
  }

  @Nonnull
  @Override
  public String getMessage() {
    return apiError.getMessage();
  }

  @Nonnull
  @Override
  public String getErrorCode() {
    return apiError.getErrorCode();
  }

  @Nonnull
  @Override
  public List<String> getFields() {
    return apiError.getFields();
  }
}
