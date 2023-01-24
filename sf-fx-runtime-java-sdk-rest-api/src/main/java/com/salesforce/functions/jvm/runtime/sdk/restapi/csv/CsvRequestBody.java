/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.csv;

import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiRequestBody;
import org.apache.http.entity.ContentType;

public class CsvRequestBody implements RestApiRequestBody {
  private final CsvTable table;

  public CsvRequestBody(CsvTable table) {
    this.table = table;
  }

  @Override
  public ContentType getContentType() {
    return ContentType.create("text/csv");
  }

  @Override
  public byte[] getRequestContents() {
    return CsvTableUtils.serialize(table);
  }
}
