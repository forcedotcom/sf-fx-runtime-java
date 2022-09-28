/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonElement;
import com.salesforce.functions.jvm.runtime.sdk.restapi.CompositeGraphRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.CreateRecordRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.DeleteRecordRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.ModifyRecordResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.QueryNextRecordsRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.QueryRecordRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.QueryRecordResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApi;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiErrorsException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.UpdateRecordRestApiRequest;
import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.RecordModificationResult;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.ReferenceId;
import com.salesforce.functions.jvm.sdk.data.UnitOfWork;
import com.salesforce.functions.jvm.sdk.data.builder.RecordBuilder;
import com.salesforce.functions.jvm.sdk.data.builder.UnitOfWorkBuilder;
import com.salesforce.functions.jvm.sdk.data.error.DataApiException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class DataApiImpl implements DataApi {
  private static final String INCOMPATIBLE_RECORD_MESSAGE =
      "Given Record is not compatible with this DataApi instance!";

  private static final String INCOMPATIBLE_UNIT_OF_WORK_MESSAGE =
      "Given UnitOfWork is not compatible with this DataApi instance!";

  private final RestApi restApi;

  public DataApiImpl(URI orgDomainUrl, String apiVersion, String accessToken) {
    this.restApi = new RestApi(orgDomainUrl, apiVersion, accessToken);
  }

  @Override
  @Nonnull
  public RecordQueryResult query(String soql) throws DataApiException {
    RestApiRequest<QueryRecordResult> request = new QueryRecordRestApiRequest(soql);

    try {
      return BinaryFieldUtil.convert(executeRequest(request), restApi);
    } catch (URISyntaxException | IOException e) {
      throw new DataApiException("Could not process API response!", e);
    }
  }

  @Override
  @Nonnull
  public RecordQueryResult queryMore(RecordQueryResult queryResult) throws DataApiException {
    RecordQueryResultImpl impl = (RecordQueryResultImpl) queryResult;

    if (impl.getNextRecordsPath().isPresent()) {
      try {
        return BinaryFieldUtil.convert(
            executeRequest(new QueryNextRecordsRestApiRequest(impl.getNextRecordsPath().get())),
            restApi);
      } catch (URISyntaxException | IOException e) {
        throw new DataApiException("Could not process API response!", e);
      }
    }

    return new EmptyRecordQueryResultImpl(impl.isDone(), impl.getTotalSize());
  }

  @Nonnull
  @Override
  public RecordModificationResult create(Record record) throws DataApiException {
    return new RecordModificationResultImpl(executeRequest(apiRequestForCreate(record)));
  }

  @Nonnull
  @Override
  public RecordModificationResult update(Record record) throws DataApiException {
    return new RecordModificationResultImpl(executeRequest(apiRequestForUpdate(record)));
  }

  @Nonnull
  @Override
  public RecordModificationResult delete(String type, String id) throws DataApiException {
    return new RecordModificationResultImpl(
        executeRequest(new DeleteRecordRestApiRequest(type, id)));
  }

  @Nonnull
  @Override
  public RecordBuilder newRecordBuilder(String type) {
    return new RecordBuilderImpl(type);
  }

  @Nonnull
  @Override
  public RecordBuilder newRecordBuilder(Record record) {
    if (!(record instanceof RecordImpl)) {
      throw new IllegalArgumentException(INCOMPATIBLE_RECORD_MESSAGE);
    }

    RecordImpl recordImpl = (RecordImpl) record;

    return new RecordBuilderImpl(recordImpl.getType(), recordImpl.getFieldValues());
  }

  @Nonnull
  @Override
  public UnitOfWorkBuilder newUnitOfWorkBuilder() {
    return new UnitOfWorkBuilderImpl();
  }

  @Override
  @Nonnull
  public Map<ReferenceId, RecordModificationResult> commitUnitOfWork(UnitOfWork unitOfWork)
      throws DataApiException {

    if (!(unitOfWork instanceof UnitOfWorkImpl)) {
      throw new IllegalArgumentException(INCOMPATIBLE_UNIT_OF_WORK_MESSAGE);
    }

    UnitOfWorkImpl impl = (UnitOfWorkImpl) unitOfWork;

    CompositeGraphRestApiRequest<ModifyRecordResult> request =
        new CompositeGraphRestApiRequest<>(
            restApi.getOrgDomainUrl(), restApi.getApiVersion(), impl.getSubrequests());

    Map<String, ModifyRecordResult> result = executeRequest(request);

    Map<ReferenceId, RecordModificationResult> actualResult = new HashMap<>();
    for (Map.Entry<String, ModifyRecordResult> entry : result.entrySet()) {
      actualResult.put(
          new ReferenceIdImpl(entry.getKey()), new RecordModificationResultImpl(entry.getValue()));
    }

    return actualResult;
  }

  @Override
  @Nonnull
  public String getAccessToken() {
    return restApi.getAccessToken();
  }

  public static RestApiRequest<ModifyRecordResult> apiRequestForUpdate(Record record) {
    if (!(record instanceof RecordImpl)) {
      throw new IllegalArgumentException(INCOMPATIBLE_RECORD_MESSAGE);
    }

    RecordImpl recordImpl = (RecordImpl) record;

    String id =
        recordImpl
            .getStringField("id")
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Given Record does not have an Id field and therefore cannot be updated."));

    Map<String, JsonElement> fieldValues = BinaryFieldUtil.convert(recordImpl.getFieldValues());
    fieldValues.remove("id");

    return new UpdateRecordRestApiRequest(id, recordImpl.getType(), fieldValues);
  }

  public static RestApiRequest<ModifyRecordResult> apiRequestForCreate(Record record) {
    if (!(record instanceof RecordImpl)) {
      throw new IllegalArgumentException(INCOMPATIBLE_RECORD_MESSAGE);
    }

    RecordImpl recordImpl = (RecordImpl) record;
    return new CreateRecordRestApiRequest(
        recordImpl.getType(), BinaryFieldUtil.convert(recordImpl.getFieldValues()));
  }

  private <T> T executeRequest(RestApiRequest<T> request) throws DataApiException {
    try {
      return restApi.execute(request);
    } catch (RestApiErrorsException restApiException) {
      throw mapException(restApiException);
    } catch (RestApiException e) {
      throw new DataApiException("Exception while executing API request!", e);
    } catch (IOException e) {
      throw new DataApiException("IOException while executing API request!", e);
    }
  }

  private static DataApiException mapException(RestApiErrorsException exception) {
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

              builder.append("Fields: ");
              builder.append(String.join(", ", error.getFields()));
              builder.append("\n");
            });

    return new DataApiException(
        builder.toString(),
        exception.getApiErrors().stream().map(DataApiErrorImpl::new).collect(Collectors.toList()));
  }
}
