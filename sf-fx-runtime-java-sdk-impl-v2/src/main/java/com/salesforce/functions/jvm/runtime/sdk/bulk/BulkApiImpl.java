/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.bulk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApi;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiRequest;
import com.salesforce.functions.jvm.sdk.Record;
import com.salesforce.functions.jvm.sdk.bulk.*;
import com.salesforce.functions.jvm.sdk.bulk.builder.JobBuilder;
import com.salesforce.functions.jvm.sdk.bulk.error.BulkApiException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class BulkApiImpl implements BulkApi {
  private final RestApi restApi;

  public BulkApiImpl(RestApi restApi) {
    this.restApi = restApi;
  }

  @Nonnull
  @Override
  public List<JobBatchResult> submit(@Nonnull Job job) {
    List<JobBatchResult> jobBatchResults = new ArrayList<>();
    for (List<Record> batch : createBatchJobIterable(job.getRecords())) {
      jobBatchResults.add(createUploadAndCloseJob(job, batch));
    }
    return jobBatchResults;
  }

  @Nonnull
  @Override
  public JobBuilder newJobBuilder(
      @Nonnull String objectType, @Nonnull Operation operation, @Nonnull Iterable<Record> records) {
    return new JobBuilderImpl(objectType, operation, records);
  }

  private JobBatchResult createUploadAndCloseJob(@Nonnull Job job, @Nonnull List<Record> records) {
    try {
      JobInfo jobInfo =
          CompletableFuture.supplyAsync(createJob(job))
              .thenCompose(uploadJobData(records))
              .thenCompose(closeJob())
              .get();
      return new JobBatchResultImpl(jobInfo.getId());
    } catch (ExecutionException | InterruptedException e) {
      if (e.getCause() instanceof UncheckedBulkApiException) {
        // TODO: log error
        UncheckedBulkApiException uncheckedBulkApiException =
            (UncheckedBulkApiException) e.getCause();
        return new JobBatchResultImpl(uncheckedBulkApiException.getCheckedException(), records);
      }
      return new JobBatchResultImpl(e, records);
    }
  }

  private Supplier<JobInfo> createJob(@Nonnull Job job) {
    return () -> {
      try {
        return executeRequest(new BulkApiRequestCreateJob(job.getObjectType(), job.getOperation()));
      } catch (BulkApiException e) {
        throw new UncheckedBulkApiException(e);
      }
    };
  }

  private Function<JobInfo, CompletionStage<JobInfo>> uploadJobData(@Nonnull List<Record> records) {
    return jobInfo ->
        CompletableFuture.supplyAsync(
            () -> {
              try {
                executeRequest(new BulkApiRequestUploadJobData(jobInfo.getId(), records));
                return jobInfo;
              } catch (BulkApiException e) {
                throw new UncheckedBulkApiException(e);
              }
            });
  }

  private Function<JobInfo, CompletionStage<JobInfo>> closeJob() {
    return jobInfo ->
        CompletableFuture.supplyAsync(
            () -> {
              try {
                return executeRequest(new BulkApiJobRequestCloseJob(jobInfo.getId()));
              } catch (BulkApiException e) {
                throw new UncheckedBulkApiException(e);
              }
            });
  }

  private <T> T executeRequest(RestApiRequest<T> request) throws BulkApiException {
    try {
      return restApi.execute(request);
    } catch (RestApiErrorsException restApiException) {
      throw mapException(restApiException);
    } catch (RestApiException e) {
      throw new BulkApiException("Exception while executing API request!", e);
    } catch (IOException e) {
      throw new BulkApiException(e.getMessage(), e);
    }
  }

  private static class UncheckedBulkApiException extends RuntimeException {
    private final BulkApiException bulkApiException;

    public UncheckedBulkApiException(BulkApiException bulkApiException) {
      super(bulkApiException);
      this.bulkApiException = bulkApiException;
    }

    public BulkApiException getCheckedException() {
      return bulkApiException;
    }
  }

  private static BulkApiException mapException(RestApiErrorsException exception) {
    StringBuilder builder = new StringBuilder("One or more API errors occurred:\n");
    exception
        .getApiErrors()
        .forEach(
            error -> {
              builder.append("\n");

              builder.append("Code: ");
              builder.append(error.getErrorCode());
              builder.append("\n");

              builder.append("Message: ");
              builder.append(error.getMessage());
              builder.append("\n");

              if (!error.getFields().isEmpty()) {
                builder.append("Fields: ");
                builder.append(String.join(", ", error.getFields()));
                builder.append("\n");
              }
            });

    return new BulkApiException(
        builder.toString(),
        exception.getApiErrors().stream().map(BulkApiErrorImpl::new).collect(Collectors.toList()));
  }

  private static class JobBatchResultImpl implements JobBatchResult {
    private final String jobId;
    private final Throwable error;
    private final Iterable<Record> unsubmittedRecords;

    public JobBatchResultImpl(String jobId) {
      this.jobId = jobId;
      this.error = null;
      this.unsubmittedRecords = Collections.emptyList();
    }

    public JobBatchResultImpl(Throwable error, Iterable<Record> unsubmittedRecords) {
      this.jobId = null;
      this.error = error;
      this.unsubmittedRecords = unsubmittedRecords;
    }

    @Override
    public Optional<String> getJobId() {
      return Optional.ofNullable(jobId);
    }

    @Override
    public Optional<Throwable> getError() {
      return Optional.ofNullable(error);
    }

    @Override
    public boolean isSuccess() {
      return jobId != null;
    }

    @Override
    public boolean isError() {
      return !isSuccess();
    }

    @Override
    public Iterable<Record> getUnsubmittedRecords() {
      return unsubmittedRecords;
    }
  }

  private static class JobImpl implements Job {
    private final String objectType;
    private final Operation operation;
    private final Iterable<Record> records;
    private final String assignmentRuleId;
    private final String externalIdFieldName;

    public JobImpl(
        String objectType,
        Operation operation,
        Iterable<Record> records,
        String assignmentRuleId,
        String externalIdFieldName) {
      this.objectType = objectType;
      this.operation = operation;
      this.records = records;
      this.assignmentRuleId = assignmentRuleId;
      this.externalIdFieldName = externalIdFieldName;
    }

    @Override
    public String getObjectType() {
      return objectType;
    }

    @Override
    public Operation getOperation() {
      return operation;
    }

    @Override
    public Optional<String> getAssignmentRuleId() {
      return Optional.of(assignmentRuleId);
    }

    @Override
    public Optional<String> getExternalIdFieldName() {
      return Optional.of(externalIdFieldName);
    }

    @Override
    public Iterable<Record> getRecords() {
      return records;
    }
  }

  private static class JobBuilderImpl implements JobBuilder {
    private final String objectType;
    private final Operation operation;
    private final Iterable<Record> records;
    private String assignmentRuleId;
    private String externalIdFieldName;

    public JobBuilderImpl(String objectType, Operation operation, Iterable<Record> records) {
      this.objectType = objectType;
      this.operation = operation;
      this.records = records;
    }

    @Nonnull
    @Override
    public JobBuilder withAssignmentRuleId(String assignmentRuleId) {
      this.assignmentRuleId = assignmentRuleId;
      return this;
    }

    @Nonnull
    @Override
    public JobBuilder withExternalIdFieldName(String externalIdFieldName) {
      this.externalIdFieldName = externalIdFieldName;
      return this;
    }

    @Nonnull
    @Override
    public Job build() {
      return new JobImpl(objectType, operation, records, assignmentRuleId, externalIdFieldName);
    }
  }

  private static Iterable<List<Record>> createBatchJobIterable(Iterable<Record> records) {
    final long maxBatchSizeInBytes = 100 * 1_000_000L; // 100MB
    final AtomicLong currentTotal = new AtomicLong(0L);
    final Iterator<Record> iterator = records.iterator();
    final List<Record> carryForward = new ArrayList<>(1);
    final AtomicBoolean includeHeaderAtomic = new AtomicBoolean(true);

    return () ->
        new Iterator<List<Record>>() {
          @Override
          public boolean hasNext() {
            return iterator.hasNext() || !carryForward.isEmpty();
          }

          @Override
          public List<Record> next() {
            List<Record> batch = new ArrayList<>();
            long total = currentTotal.get();
            boolean includeHeader = includeHeaderAtomic.get();

            for (Record record : carryForward) {
              long newTotal = total + calculateCsvRowSize(record, includeHeader);
              if (includeHeader) {
                includeHeader = false;
                includeHeaderAtomic.set(false);
              }
              batch.add(record);
              total = newTotal;
            }

            while (total < maxBatchSizeInBytes && iterator.hasNext()) {
              Record record = iterator.next();
              long newTotal = total + calculateCsvRowSize(record, includeHeader);
              if (includeHeader) {
                includeHeader = false;
                includeHeaderAtomic.set(false);
              }
              if (newTotal > maxBatchSizeInBytes) {
                carryForward.add(record);
              } else {
                batch.add(record);
              }
              total = newTotal;
            }

            currentTotal.set(0);
            includeHeaderAtomic.set(true);

            return batch;
          }
        };
  }

  private static long calculateCsvRowSize(Record record, boolean withHeader) {
    try {
      StringWriter stringWriter = new StringWriter();
      String[] fieldNames = record.getFieldNames().stream().sorted().toArray(String[]::new);
      CSVPrinter csvPrinter =
          CSVFormat.RFC4180
              .builder()
              .setHeader(fieldNames)
              .setSkipHeaderRecord(!withHeader)
              .build()
              .print(stringWriter);
      List<Object> values =
          Arrays.stream(fieldNames)
              .sequential()
              .map(fieldName -> record.getStringField(fieldName).orElse(""))
              .collect(Collectors.toList());
      csvPrinter.printRecord(values);
      return stringWriter.toString().getBytes().length;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
