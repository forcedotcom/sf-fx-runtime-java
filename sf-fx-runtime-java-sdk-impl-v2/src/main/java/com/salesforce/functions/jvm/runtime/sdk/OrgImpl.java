/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.sdk.bulk.BulkApiImpl;
import com.salesforce.functions.jvm.runtime.sdk.restapi.RestApi;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.Record;
import com.salesforce.functions.jvm.sdk.RecordBuilder;
import com.salesforce.functions.jvm.sdk.User;
import com.salesforce.functions.jvm.sdk.bulk.BulkApi;
import java.net.URI;
import javax.annotation.Nonnull;

public class OrgImpl implements Org {
  private final String apiVersion;
  private final SalesforceContextCloudEventExtension salesforceContext;
  private final UserImpl user;
  private final DataApiImpl dataApi;
  private final BulkApiImpl bulkApi;

  private static final String INCOMPATIBLE_RECORD_MESSAGE =
      "Given QueryRecord is not compatible with this DataApi instance!";

  public OrgImpl(
      SalesforceContextCloudEventExtension salesforceContext,
      SalesforceFunctionContextCloudEventExtension functionContext,
      String apiVersion) {
    this.apiVersion = apiVersion;
    this.salesforceContext = salesforceContext;
    this.dataApi =
        new DataApiImpl(this.getBaseUrl(), this.getApiVersion(), functionContext.getAccessToken());
    this.bulkApi =
        new BulkApiImpl(
            new RestApi(this.getBaseUrl(), this.getApiVersion(), functionContext.getAccessToken()));
    this.user = new UserImpl(salesforceContext);
  }

  @Override
  @Nonnull
  public String getId() {
    return salesforceContext.getUserContext().getOrgId();
  }

  @Override
  @Nonnull
  public URI getBaseUrl() {
    return salesforceContext.getUserContext().getSalesforceBaseUrl();
  }

  @Override
  @Nonnull
  public URI getDomainUrl() {
    return salesforceContext.getUserContext().getOrgDomainUrl();
  }

  @Override
  @Nonnull
  public String getApiVersion() {
    return this.apiVersion;
  }

  @Override
  @Nonnull
  public DataApiImpl getDataApi() {
    return dataApi;
  }

  @Override
  @Nonnull
  public User getUser() {
    return user;
  }

  @Override
  @Nonnull
  public BulkApi getBulkApi() {
    return bulkApi;
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
}
