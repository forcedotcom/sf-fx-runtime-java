/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RestApiError {
  private final String message;
  private final String errorCode;
  private final List<String> fields;

  public RestApiError(String message, String errorCode, List<String> fields) {
    this.message = message;
    this.errorCode = errorCode;
    this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
  }

  public String getMessage() {
    return message;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public List<String> getFields() {
    return fields;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestApiError that = (RestApiError) o;
    return Objects.equals(message, that.message)
        && Objects.equals(errorCode, that.errorCode)
        && Objects.equals(fields, that.fields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, errorCode, fields);
  }
}
