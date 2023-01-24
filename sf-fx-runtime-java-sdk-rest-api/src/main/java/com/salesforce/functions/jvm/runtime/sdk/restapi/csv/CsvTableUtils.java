/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.csv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public final class CsvTableUtils {

  public static CsvTable deserialize(byte[] data) throws CsvException {
    try {
      CSVFormat format =
          CSVFormat.Builder.create(CSVFormat.DEFAULT).setRecordSeparator('\n').build();

      CSVParser parser =
          CSVParser.parse(new ByteArrayInputStream(data), StandardCharsets.UTF_8, format);

      List<List<String>> rows =
          parser.getRecords().stream().map(CSVRecord::toList).collect(Collectors.toList());

      return new CsvTable(rows.get(0), rows.subList(1, rows.size()));
    } catch (IOException e) {
      throw new CsvException(e);
    }
  }

  public static byte[] serialize(CsvTable table) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    CSVFormat csvFormat =
        CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setHeader(table.getHeaders().toArray(new String[0]))
            .setRecordSeparator('\n')
            .build();

    try (CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), csvFormat)) {
      for (List<String> row : table.getRows()) {
        csvPrinter.printRecord(row);
      }
    } catch (IOException e) {
      // The CSV library is designed to work with IO streams directly. Since we write into memory,
      // IO exceptions cannot occur.
      throw new IllegalStateException("Unexpected IOException while serializing CSV data!", e);
    }

    return outputStream.toByteArray();
  }
}
