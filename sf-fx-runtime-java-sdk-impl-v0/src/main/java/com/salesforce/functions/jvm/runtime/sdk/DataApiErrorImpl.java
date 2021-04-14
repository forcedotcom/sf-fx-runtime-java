/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiError;
import com.salesforce.functions.jvm.sdk.data.DataApiError;
import java.util.List;
import javax.annotation.Nonnull;

public class DataApiErrorImpl implements DataApiError {
  private final RestApiError apiError;

  public DataApiErrorImpl(RestApiError apiError) {
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
