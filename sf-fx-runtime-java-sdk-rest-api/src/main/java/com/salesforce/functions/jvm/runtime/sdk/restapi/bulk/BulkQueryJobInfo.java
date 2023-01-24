/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi.bulk;

import com.google.gson.annotations.JsonAdapter;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class BulkQueryJobInfo {
  private final String id;
  private final BulkQueryOperation operation;
  private final String object;
  private final String createdById;

  @JsonAdapter(SalesforceDateTimeStringJsonAdapter.class)
  private final Instant createdDate;

  @JsonAdapter(SalesforceDateTimeStringJsonAdapter.class)
  private final Instant systemModstamp;

  private final BulkJobState state;
  private final ContentType contentType;
  private final String apiVersion;
  private final LineEnding lineEnding;
  private final ColumnDelimiter columnDelimiter;
  private final long numberRecordsProcessed;
  private final long retries;

  @JsonAdapter(DurationJsonAdapter.class)
  private final Duration totalProcessingTime;

  public BulkQueryJobInfo(
      String id,
      BulkQueryOperation operation,
      String object,
      String createdById,
      Instant createdDate,
      Instant systemModstamp,
      BulkJobState state,
      ContentType contentType,
      String apiVersion,
      LineEnding lineEnding,
      ColumnDelimiter columnDelimiter,
      long numberRecordsProcessed,
      long retries,
      Duration totalProcessingTime) {
    this.id = id;
    this.operation = operation;
    this.object = object;
    this.createdById = createdById;
    this.createdDate = createdDate;
    this.systemModstamp = systemModstamp;
    this.state = state;
    this.contentType = contentType;
    this.apiVersion = apiVersion;
    this.lineEnding = lineEnding;
    this.columnDelimiter = columnDelimiter;
    this.numberRecordsProcessed = numberRecordsProcessed;
    this.retries = retries;
    this.totalProcessingTime = totalProcessingTime;
  }

  @Override
  public String toString() {
    return "BulkQueryJobInfo{"
        + "id='"
        + id
        + '\''
        + ", operation="
        + operation
        + ", object='"
        + object
        + '\''
        + ", createdById='"
        + createdById
        + '\''
        + ", createdDate="
        + createdDate
        + ", systemModstamp="
        + systemModstamp
        + ", state="
        + state
        + ", contentType="
        + contentType
        + ", apiVersion='"
        + apiVersion
        + '\''
        + ", lineEnding="
        + lineEnding
        + ", columnDelimiter="
        + columnDelimiter
        + ", numberRecordsProcessed="
        + numberRecordsProcessed
        + ", retries="
        + retries
        + ", totalProcessingTime="
        + totalProcessingTime
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BulkQueryJobInfo that = (BulkQueryJobInfo) o;
    return numberRecordsProcessed == that.numberRecordsProcessed
        && retries == that.retries
        && Objects.equals(id, that.id)
        && operation == that.operation
        && Objects.equals(object, that.object)
        && Objects.equals(createdById, that.createdById)
        && Objects.equals(createdDate, that.createdDate)
        && Objects.equals(systemModstamp, that.systemModstamp)
        && state == that.state
        && contentType == that.contentType
        && Objects.equals(apiVersion, that.apiVersion)
        && lineEnding == that.lineEnding
        && columnDelimiter == that.columnDelimiter
        && Objects.equals(totalProcessingTime, that.totalProcessingTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        operation,
        object,
        createdById,
        createdDate,
        systemModstamp,
        state,
        contentType,
        apiVersion,
        lineEnding,
        columnDelimiter,
        numberRecordsProcessed,
        retries,
        totalProcessingTime);
  }
}
