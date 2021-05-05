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
  @SerializedName("isFunctionError")
  private final boolean isFunctionError;

  @Expose
  @SerializedName("stacktrace")
  @JsonAdapter(StackTraceElementListJsonSerializer.class)
  private final List<StackTraceElement> stacktrace;

  public ExtraInfo() {
    this.requestId = "n/a";
    this.source = "n/a";
    this.executionTime = Duration.ZERO;
    this.isFunctionError = false;
    this.stacktrace = new ArrayList<>();
  }

  public ExtraInfo(
      String requestId,
      String source,
      Duration executionTime,
      boolean isFunctionError,
      List<StackTraceElement> stacktrace) {
    this.requestId = requestId;
    this.source = source;
    this.executionTime = executionTime;
    this.isFunctionError = isFunctionError;
    this.stacktrace = stacktrace;
  }

  public ExtraInfo withCloudEventData(CloudEvent cloudEvent) {
    return new ExtraInfo(
        cloudEvent.getId(),
        cloudEvent.getSource().toString(),
        this.executionTime,
        this.isFunctionError,
        this.stacktrace);
  }

  public ExtraInfo withInternalExceptionData(Throwable e) {
    return new ExtraInfo(
        this.requestId,
        this.source,
        this.executionTime,
        false,
        Collections.unmodifiableList(Arrays.asList(e.getStackTrace())));
  }

  public ExtraInfo withFunctionExceptionData(Throwable e) {
    return new ExtraInfo(
        this.requestId,
        this.source,
        this.executionTime,
        true,
        Collections.unmodifiableList(Arrays.asList(e.getStackTrace())));
  }

  public ExtraInfo withFunctionExceptionData(FunctionThrewExceptionException e) {
    return new ExtraInfo(
        this.requestId,
        this.source,
        this.executionTime,
        true,
        Collections.unmodifiableList(e.getFunctionStackTrace()));
  }

  public ExtraInfo withFunctionExecutionTime(Duration duration) {
    return new ExtraInfo(
        this.requestId, this.source, duration, this.isFunctionError, this.stacktrace);
  }
}
