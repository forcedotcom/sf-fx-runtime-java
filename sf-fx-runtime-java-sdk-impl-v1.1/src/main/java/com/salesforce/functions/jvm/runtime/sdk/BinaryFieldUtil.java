/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.salesforce.functions.jvm.runtime.sdk.restapi.QueryRecordResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.Record;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApi;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.*;

final class BinaryFieldUtil {
  public static boolean isBinaryField(String objectType, String fieldName) {
    Map<String, List<String>> mapping = new HashMap<>();
    mapping.put("ContentVersion", Collections.singletonList("VersionData"));

    return Optional.ofNullable(mapping.get(objectType))
        .map(fieldList -> fieldList.contains(fieldName))
        .orElse(false);
  }

  public static RecordQueryResultImpl convert(QueryRecordResult queryRecordResult, RestApi restApi)
      throws URISyntaxException, IOException {
    List<RecordWithSubQueryResultsImpl> records = new ArrayList<>();
    for (Record record : queryRecordResult.getRecords()) {
      records.add(convert(record, restApi));
    }

    return new RecordQueryResultImpl(
        queryRecordResult.isDone(),
        queryRecordResult.getTotalSize(),
        queryRecordResult.getNextRecordsPath(),
        records);
  }

  public static RecordWithSubQueryResultsImpl convert(Record record, RestApi restApi)
      throws URISyntaxException, IOException {
    final String recordObjectType = record.getAttributes().get("type").getAsString();

    final Map<String, FieldValue> fieldValues = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (Map.Entry<String, JsonPrimitive> fieldEntry : record.getValues().entrySet()) {
      FieldValue fieldValue = new FieldValue(fieldEntry.getValue());

      if (isBinaryField(recordObjectType, fieldEntry.getKey())) {
        ByteBuffer data = restApi.downloadFile(fieldEntry.getValue().getAsString());
        fieldValue = new FieldValue(data);
      }

      fieldValues.put(fieldEntry.getKey(), fieldValue);
    }

    final Map<String, RecordQueryResultImpl> subQueryResults = new HashMap<>();
    for (Map.Entry<String, QueryRecordResult> entry : record.getSubQueryResults().entrySet()) {
      subQueryResults.put(entry.getKey(), convert(entry.getValue(), restApi));
    }

    return new RecordWithSubQueryResultsImpl(recordObjectType, fieldValues, subQueryResults);
  }

  public static Map<String, JsonElement> convert(Map<String, FieldValue> fieldValues2) {
    Map<String, JsonElement> fieldValues = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (Map.Entry<String, FieldValue> entry : fieldValues2.entrySet()) {
      if (entry.getValue().isBinaryData()) {
        fieldValues.put(
            entry.getKey(),
            new JsonPrimitive(
                Base64.getEncoder().encodeToString(entry.getValue().getBinaryData().array())));
      } else {
        fieldValues.put(entry.getKey(), entry.getValue().getJsonData());
      }
    }

    return fieldValues;
  }

  private BinaryFieldUtil() {}
}
