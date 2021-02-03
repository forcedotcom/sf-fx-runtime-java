/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.JsonPrimitive;

import java.util.*;

public final class QueryRecordResult {
    private final long totalSize;
    private final boolean done;
    private final List<Record> records;
    private final String nextRecordsPath;

    public QueryRecordResult(long totalSize, boolean done, List<Record> records, String nextRecordsPath) {
        this.totalSize = totalSize;
        this.done = done;
        this.records = new ArrayList<>(records);
        this.nextRecordsPath = nextRecordsPath;
    }

    public List<Record> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public long getTotalSize() {
        return totalSize;
    }

    public boolean isDone() {
        return done;
    }

    public Optional<String> getNextRecordsPath() {
        return Optional.ofNullable(nextRecordsPath);
    }

    public static final class Record {
        private final Map<String, JsonPrimitive> attributes;
        private final Map<String, JsonPrimitive> values;

        public Record(Map<String, JsonPrimitive> attributes, Map<String, JsonPrimitive> values) {
            this.attributes = attributes;
            this.values = values;
        }

        public Map<String, JsonPrimitive> getAttributes() {
            return Collections.unmodifiableMap(attributes);
        }

        public Map<String, JsonPrimitive> getValues() {
            return Collections.unmodifiableMap(values);
        }
    }
}
