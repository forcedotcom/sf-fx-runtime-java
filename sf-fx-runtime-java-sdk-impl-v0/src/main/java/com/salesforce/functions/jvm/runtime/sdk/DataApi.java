/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.*;
import com.salesforce.functions.jvm.sdk.data.DataApiException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class DataApi implements com.salesforce.functions.jvm.sdk.data.DataApi {
  private final RestApi restApi;

  public DataApi(URI salesforceBaseUrl, String apiVersion, String accessToken) {
    this.restApi = new RestApi(salesforceBaseUrl, apiVersion, accessToken);
  }

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.data.RecordQueryResult query(String soql)
      throws DataApiException {
    RestApiRequest<QueryRecordResult> request = new QueryRecordRestApiRequest(soql);
    try {
      return new RecordQueryResult(restApi.execute(request));
    } catch (IOException e) {
      throw new DataApiException("I/O error during query", e);
    }
  }

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.data.RecordModificationResult create(
      com.salesforce.functions.jvm.sdk.data.RecordCreate create) throws DataApiException {
    RecordCreate impl = (RecordCreate) create;

    CreateRecordRestApiRequest request =
        new CreateRecordRestApiRequest(impl.getType(), impl.getValues());
    try {
      return new RecordModificationResult(restApi.execute(request));
    } catch (IOException e) {
      throw new DataApiException("I/O error during create", e);
    }
  }

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.data.RecordModificationResult update(
      com.salesforce.functions.jvm.sdk.data.RecordUpdate update) throws DataApiException {
    RecordUpdate impl = (RecordUpdate) update;

    UpdateRecordRestApiRequest request =
        new UpdateRecordRestApiRequest(impl.getId(), impl.getType(), impl.getValues());
    try {
      return new RecordModificationResult(restApi.execute(request));
    } catch (IOException e) {
      throw new DataApiException("I/O error during update", e);
    }
  }

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.data.RecordQueryResult queryMore(
      com.salesforce.functions.jvm.sdk.data.RecordQueryResult queryResult) throws DataApiException {
    RecordQueryResult impl = (RecordQueryResult) queryResult;

    QueryNextRecordsRestApiRequest request =
        new QueryNextRecordsRestApiRequest(impl.getNextRecordsPath().get());
    try {
      return new RecordQueryResult(restApi.execute(request));
    } catch (IOException e) {
      throw new DataApiException("I/O error during query more", e);
    }
  }

  @Override
  @Nonnull
  public Map<
          com.salesforce.functions.jvm.sdk.data.ReferenceId,
          com.salesforce.functions.jvm.sdk.data.RecordModificationResult>
      commitUnitOfWork(com.salesforce.functions.jvm.sdk.data.UnitOfWork unitOfWork)
          throws DataApiException {
    UnitOfWork impl = (UnitOfWork) unitOfWork;

    CompositeRestApiRequest<ModifyRecordResult> request =
        new CompositeRestApiRequest<>(
            restApi.getSalesforceBaseUrl(), restApi.getApiVersion(), impl.getSubrequests());

    Map<String, ModifyRecordResult> result;
    try {
      result = restApi.execute(request);
    } catch (IOException e) {
      throw new DataApiException("I/O error while committing UnitOfWork", e);
    }

    Map<
            com.salesforce.functions.jvm.sdk.data.ReferenceId,
            com.salesforce.functions.jvm.sdk.data.RecordModificationResult>
        actualResult = new HashMap<>();
    for (Map.Entry<String, ModifyRecordResult> stringModifyRecordResultEntry : result.entrySet()) {
      actualResult.put(
          new ReferenceId(stringModifyRecordResultEntry.getKey()),
          new RecordModificationResult(stringModifyRecordResultEntry.getValue()));
    }

    return actualResult;
  }

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.data.UnitOfWork newUnitOfWork() {
    return new UnitOfWork();
  }

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.data.RecordCreate newRecordCreate(String type) {
    return new RecordCreate(type, new HashMap<>());
  }

  @Override
  @Nonnull
  public com.salesforce.functions.jvm.sdk.data.RecordUpdate newRecordUpdate(
      String type, String id) {
    return new RecordUpdate(type, id, new HashMap<>());
  }

  @Override
  @Nonnull
  public String getAccessToken() {
    return restApi.getAccessToken();
  }
}
