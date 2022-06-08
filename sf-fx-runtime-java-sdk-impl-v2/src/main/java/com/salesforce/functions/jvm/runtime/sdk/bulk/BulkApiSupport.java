/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.salesforce.functions.jvm.sdk.bulk.JobState;
import com.salesforce.functions.jvm.sdk.bulk.Operation;
import java.io.IOException;
import java.util.function.Function;

public class BulkApiSupport {
  public static final Function<GsonBuilder, GsonBuilder> bulkApiGsonBuilder =
      gsonBuilder ->
          gsonBuilder
              .registerTypeAdapter(Operation.class, new OperationAdapter())
              .registerTypeAdapter(JobState.class, new JobStateAdapter());

  private static class OperationAdapter extends TypeAdapter<Operation> {
    @Override
    public void write(JsonWriter out, Operation value) throws IOException {
      if (value != null) {
        out.value(value.getTextValue());
      } else {
        out.nullValue();
      }
    }

    @Override
    public Operation read(JsonReader in) throws IOException {
      String value = in.nextString();
      return value != null ? Operation.fromTextValue(value) : null;
    }
  }

  private static class JobStateAdapter extends TypeAdapter<JobState> {
    @Override
    public void write(JsonWriter out, JobState value) throws IOException {
      if (value != null) {
        out.value(value.getTextValue());
      } else {
        out.nullValue();
      }
    }

    @Override
    public JobState read(JsonReader in) throws IOException {
      String value = in.nextString();
      return value != null ? JobState.fromTextValue(value) : null;
    }
  }
}
