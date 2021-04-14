/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.CompositeRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.CreateRecordRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.ModifyRecordResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.QueryNextRecordsRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.QueryRecordRestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.QueryRecordResult;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApi;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiException;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApiRequest;
import com.salesforce.functions.jvm.runtime.sdk.restapi.UpdateRecordRestApiRequest;
import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.DataApiException;
import com.salesforce.functions.jvm.sdk.data.RecordCreate;
import com.salesforce.functions.jvm.sdk.data.RecordModificationResult;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.RecordUpdate;
import com.salesforce.functions.jvm.sdk.data.ReferenceId;
import com.salesforce.functions.jvm.sdk.data.UnitOfWork;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class DataApiImpl implements DataApi {
  private final RestApi restApi;

  public DataApiImpl(URI salesforceBaseUrl, String apiVersion, String accessToken) {
    this.restApi = new RestApi(salesforceBaseUrl, apiVersion, accessToken);
  }

  @Override
  @Nonnull
  public RecordQueryResultImpl query(String soql) throws DataApiException {
    RestApiRequest<QueryRecordResult> request = new QueryRecordRestApiRequest(soql);
    try {
      return new RecordQueryResultImpl(restApi.execute(request));
    } catch (IOException e) {
      throw new DataApiException("I/O error during query", e);
    } catch (RestApiException e) {
      throw mapException(e);
    }
  }

  @Override
  @Nonnull
  public RecordQueryResultImpl queryMore(RecordQueryResult queryResult) throws DataApiException {
    RecordQueryResultImpl impl = (RecordQueryResultImpl) queryResult;

    QueryNextRecordsRestApiRequest request =
        new QueryNextRecordsRestApiRequest(impl.getNextRecordsPath().get());

    try {
      return new RecordQueryResultImpl(restApi.execute(request));
    } catch (IOException e) {
      throw new DataApiException("I/O error during queryMore", e);
    } catch (RestApiException e) {
      throw mapException(e);
    }
  }

  @Override
  @Nonnull
  public RecordModificationResult create(RecordCreate create) throws DataApiException {
    RecordCreateImpl impl = (RecordCreateImpl) create;

    CreateRecordRestApiRequest request =
        new CreateRecordRestApiRequest(impl.getType(), impl.getValues());
    try {
      return new RecordModificationResultImpl(restApi.execute(request));
    } catch (IOException e) {
      throw new DataApiException("I/O error during create", e);
    } catch (RestApiException e) {
      throw mapException(e);
    }
  }

  @Override
  @Nonnull
  public RecordModificationResult update(RecordUpdate update) throws DataApiException {
    RecordUpdateImpl impl = (RecordUpdateImpl) update;

    UpdateRecordRestApiRequest request =
        new UpdateRecordRestApiRequest(impl.getId(), impl.getType(), impl.getValues());
    try {
      return new RecordModificationResultImpl(restApi.execute(request));
    } catch (IOException e) {
      throw new DataApiException("I/O error during update", e);
    } catch (RestApiException e) {
      throw mapException(e);
    }
  }

  @Override
  @Nonnull
  public Map<ReferenceId, RecordModificationResult> commitUnitOfWork(UnitOfWork unitOfWork)
      throws DataApiException {
    UnitOfWorkImpl impl = (UnitOfWorkImpl) unitOfWork;

    CompositeRestApiRequest<ModifyRecordResult> request =
        new CompositeRestApiRequest<>(
            restApi.getSalesforceBaseUrl(), restApi.getApiVersion(), impl.getSubrequests());

    Map<String, ModifyRecordResult> result;
    try {
      result = restApi.execute(request);
    } catch (IOException e) {
      throw new DataApiException("I/O error during commitUnitOfWork", e);
    } catch (RestApiException e) {
      throw mapException(e);
    }

    Map<ReferenceId, RecordModificationResult> actualResult = new HashMap<>();
    for (Map.Entry<String, ModifyRecordResult> stringModifyRecordResultEntry : result.entrySet()) {
      actualResult.put(
          new ReferenceIdImpl(stringModifyRecordResultEntry.getKey()),
          new RecordModificationResultImpl(stringModifyRecordResultEntry.getValue()));
    }

    return actualResult;
  }

  @Override
  @Nonnull
  public UnitOfWork newUnitOfWork() {
    return new UnitOfWorkImpl();
  }

  @Override
  @Nonnull
  public RecordCreate newRecordCreate(String type) {
    return new RecordCreateImpl(type, new HashMap<>());
  }

  @Override
  @Nonnull
  public RecordUpdate newRecordUpdate(String type, String id) {
    return new RecordUpdateImpl(type, id, new HashMap<>());
  }

  @Override
  @Nonnull
  public String getAccessToken() {
    return restApi.getAccessToken();
  }

  private static DataApiException mapException(RestApiException exception) {
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
