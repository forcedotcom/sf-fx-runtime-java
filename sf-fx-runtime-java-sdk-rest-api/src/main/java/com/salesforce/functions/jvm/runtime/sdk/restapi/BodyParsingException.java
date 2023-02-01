/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

public class BodyParsingException extends Exception {
  public BodyParsingException() {
    super();
  }

  public BodyParsingException(String message) {
    super(message);
  }

  public BodyParsingException(String message, Throwable cause) {
    super(message, cause);
  }

  public BodyParsingException(Throwable cause) {
    super(cause);
  }
}
