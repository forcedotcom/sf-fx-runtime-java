/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.sdk.restapi.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DataApi implements com.salesforce.functions.jvm.sdk.data.DataApi {
    private final RestApi restApi;

    public DataApi(URI salesforceBaseUrl, String apiVersion, String accessToken) {
        this.restApi = new RestApi(salesforceBaseUrl, apiVersion, accessToken);
    }

    @Override
    public com.salesforce.functions.jvm.sdk.data.RecordQueryResult query(String soql) throws IOException {
        RestApiRequest<QueryRecordResult> request = new QueryRecordRestApiRequest(soql);
        return new RecordQueryResult(restApi.execute(request));
    }

    @Override
    public com.salesforce.functions.jvm.sdk.data.RecordModificationResult insert(com.salesforce.functions.jvm.sdk.data.RecordInsert insert) throws IOException {
        RecordInsert impl = (RecordInsert) insert;

        CreateRecordRestApiRequest request = new CreateRecordRestApiRequest(impl.getType(), impl.getValues());
        return new RecordModificationResult(restApi.execute(request));
    }

    @Override
    public com.salesforce.functions.jvm.sdk.data.RecordModificationResult update(com.salesforce.functions.jvm.sdk.data.RecordUpdate update) throws IOException {
        RecordUpdate impl = (RecordUpdate) update;

        UpdateRecordRestApiRequest request
                = new UpdateRecordRestApiRequest(impl.getId(), impl.getType(), impl.getValues());
        return new RecordModificationResult(restApi.execute(request));
    }

    @Override
    public com.salesforce.functions.jvm.sdk.data.RecordQueryResult queryMore(com.salesforce.functions.jvm.sdk.data.RecordQueryResult queryResult) throws IOException {
        RecordQueryResult impl = (RecordQueryResult) queryResult;

        QueryNextRecordsRestApiRequest request = new QueryNextRecordsRestApiRequest(impl.getNextRecordsPath().get());
        return new RecordQueryResult(restApi.execute(request));
    }

    @Override
    public Map<com.salesforce.functions.jvm.sdk.data.ReferenceId, com.salesforce.functions.jvm.sdk.data.RecordModificationResult> commitUnitOfWork(com.salesforce.functions.jvm.sdk.data.UnitOfWork unitOfWork) throws IOException {
        UnitOfWork impl = (UnitOfWork) unitOfWork;

        CompositeRestApiRequest<ModifyRecordResult> request
                = new CompositeRestApiRequest<>(restApi.getSalesforceBaseUrl(), restApi.getApiVersion(), impl.getSubrequests());

        Map<String, ModifyRecordResult> result = restApi.execute(request);

        Map<com.salesforce.functions.jvm.sdk.data.ReferenceId, com.salesforce.functions.jvm.sdk.data.RecordModificationResult> actualResult = new HashMap<>();
        for (Map.Entry<String, ModifyRecordResult> stringModifyRecordResultEntry : result.entrySet()) {
            actualResult.put(
                    new ReferenceId(stringModifyRecordResultEntry.getKey()),
                    new RecordModificationResult(stringModifyRecordResultEntry.getValue())
            );
        }

        return actualResult;
    }

    @Override
    public com.salesforce.functions.jvm.sdk.data.UnitOfWork newUnitOfWork() {
        return new UnitOfWork();
    }

    @Override
    public com.salesforce.functions.jvm.sdk.data.RecordInsert newRecordInsert(String type) {
        return new RecordInsert(type, new HashMap<>());
    }

    @Override
    public com.salesforce.functions.jvm.sdk.data.RecordUpdate newRecordUpdate(String type, String id) {
        return new RecordUpdate(type, id, new HashMap<>());
    }

    @Override
    public String getAccessToken() {
        return restApi.getAccessToken();
    }
}
