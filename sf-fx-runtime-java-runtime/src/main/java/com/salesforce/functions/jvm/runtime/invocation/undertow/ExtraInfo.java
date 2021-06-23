/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.invocation.undertow;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionThrewExceptionException;
import io.cloudevents.CloudEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExtraInfo {
  @Expose
  @SerializedName("requestId")
  private final String requestId;

  @Expose
  @SerializedName("source")
  private final String source;

  @Expose
  @SerializedName("execTimeMs")
  @JsonAdapter(DurationAsMillisNumberJsonSerializer.class)
  private final Duration executionTime;

  @Expose
  @SerializedName("statusCode")
  private final int statusCode;

  @Expose
  @SerializedName("isFunctionError")
  private final boolean isFunctionError;

  @Expose
  @SerializedName("stack")
  @JsonAdapter(StackTraceElementListJsonSerializer.class)
  private final List<StackTraceElement> stacktrace;

  public ExtraInfo() {
    this.requestId = "n/a";
    this.source = "n/a";
    this.executionTime = Duration.ZERO;
    this.statusCode = 200;
    this.isFunctionError = false;
    this.stacktrace = new ArrayList<>();
  }

  public ExtraInfo(
      String requestId,
      String source,
      Duration executionTime,
      int statusCode,
      boolean isFunctionError,
      List<StackTraceElement> stacktrace) {
    this.requestId = requestId;
    this.source = source;
    this.executionTime = executionTime;
    this.statusCode = statusCode;
    this.isFunctionError = isFunctionError;
    this.stacktrace = stacktrace;
  }

  public ExtraInfo withCloudEventData(CloudEvent cloudEvent) {
    return new ExtraInfo(
        cloudEvent.getId(),
        cloudEvent.getSource().toString(),
        this.executionTime,
        this.statusCode,
        this.isFunctionError,
        this.stacktrace);
  }

  public ExtraInfo withInternalExceptionData(Throwable e) {
    return new ExtraInfo(
        this.requestId,
        this.source,
        this.executionTime,
        this.statusCode,
        false,
        Collections.unmodifiableList(Arrays.asList(e.getStackTrace())));
  }

  public ExtraInfo withFunctionExceptionData(Throwable e) {
    return new ExtraInfo(
        this.requestId,
        this.source,
        this.executionTime,
        this.statusCode,
        true,
        Collections.unmodifiableList(Arrays.asList(e.getStackTrace())));
  }

  public ExtraInfo withFunctionExceptionData(FunctionThrewExceptionException e) {
    return new ExtraInfo(
        this.requestId,
        this.source,
        this.executionTime,
        this.statusCode,
        true,
        Collections.unmodifiableList(e.getFunctionStackTrace()));
  }

  public ExtraInfo withFunctionExecutionTime(Duration duration) {
    return new ExtraInfo(
        this.requestId,
        this.source,
        duration,
        this.statusCode,
        this.isFunctionError,
        this.stacktrace);
  }

  public ExtraInfo withStatusCode(int statusCode) {
    return new ExtraInfo(
        this.requestId,
        this.source,
        this.executionTime,
        statusCode,
        this.isFunctionError,
        this.stacktrace);
  }

  public String getRequestId() {
    return requestId;
  }

  public String getSource() {
    return source;
  }

  public Duration getExecutionTime() {
    return executionTime;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public boolean isFunctionError() {
    return isFunctionError;
  }

  public List<StackTraceElement> getStacktrace() {
    return Collections.unmodifiableList(stacktrace);
  }
}
