/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestApiErrorsException extends Exception {
  private final List<RestApiError> apiErrors;

  public RestApiErrorsException(List<RestApiError> apiErrors) {
    this.apiErrors = Collections.unmodifiableList(new ArrayList<>(apiErrors));
  }

  public List<RestApiError> getApiErrors() {
    return apiErrors;
  }
}
