/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.csv;

import com.salesforce.functions.jvm.runtime.sdk.restapi.*;

public abstract class CsvResponseRestApiRequest<T, A extends RestApiRequestBody>
    implements RestApiRequest<T, A, CsvTable> {

  @Override
  public CsvTable parseBody(byte[] body) throws BodyParsingException {
    try {
      return CsvTableUtils.deserialize(body);
    } catch (CsvException e) {
      throw new BodyParsingException("Could not parse body as CSV!", e);
    }
  }
}
