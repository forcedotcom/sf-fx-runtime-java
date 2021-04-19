/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.salesforce.functions.jvm.sdk.data.DataApiException;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.RecordModificationResult;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.ReferenceId;
import com.salesforce.functions.jvm.sdk.data.UnitOfWork;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class DataApiImplTest {
  @Rule public WireMockRule wireMock = new WireMockRule();

  private final DataApiImpl dataApi =
      new DataApiImpl(
          URI.create("http://localhost:8080/"),
          "51.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void testQuery() throws DataApiException {
    RecordQueryResult result = dataApi.query("SELECT Name FROM Account");
    assertThat(result.isDone(), is(true));
    assertThat(result.getTotalSize(), is(5L));

    List<Record> records = result.getRecords();
    assertThat(
        records.get(0).getStringValue("Name"),
        optionalWithValue(equalTo("An awesome test account")));

    assertThat(records.get(1).getStringValue("Name"), optionalWithValue(equalTo("Global Media")));

    assertThat(records.get(2).getStringValue("Name"), optionalWithValue(equalTo("Acme")));

    assertThat(records.get(3).getStringValue("Name"), optionalWithValue(equalTo("salesforce.com")));

    assertThat(
        records.get(4).getStringValue("Name"),
        optionalWithValue(equalTo("Sample Account for Entitlements")));
  }

  @Test
  public void testQueryMore() throws DataApiException {
    RecordQueryResult result = dataApi.query("SELECT RANDOM_1__c, RANDOM_2__c FROM Random__c");
    assertThat(result.isDone(), is(false));
    assertThat(result.getTotalSize(), is(10000L));

    RecordQueryResult result2 = dataApi.queryMore(result);
    assertThat(result2.isDone(), is(false));
    assertThat(result2.getTotalSize(), is(10000L));
  }

  @Test
  public void testQueryMoreWithDoneResult() throws DataApiException {
    RecordQueryResult result = dataApi.query("SELECT Name FROM Account");
    assertThat(result.isDone(), is(true));

    RecordQueryResult result2 = dataApi.queryMore(result);
    assertThat(result2.isDone(), is(result.isDone()));
    assertThat(result2.getTotalSize(), is(result.getTotalSize()));
    assertThat(result2.getRecords(), is(empty()));
  }

  @Test
  public void testQueryWithMalformedSoql() {
    try {
      dataApi.query("SELEKT Name FROM Account");
    } catch (DataApiException e) {
      assertThat(
          e.getMessage(),
          equalTo(
              "One or more API errors occurred:\n\nCode: MALFORMED_QUERY\nMessage: unexpected token: SELEKT\nFields: \n"));

      assertThat(e.getDataApiErrors(), hasSize(1));
      assertThat(e.getDataApiErrors().get(0).getErrorCode(), equalTo("MALFORMED_QUERY"));
      assertThat(e.getDataApiErrors().get(0).getMessage(), equalTo("unexpected token: SELEKT"));
      assertThat(e.getDataApiErrors().get(0).getFields(), is(empty()));
      return;
    }

    Assert.fail("Expected Exception!");
  }

  @Test
  public void testCreate() throws DataApiException {
    RecordModificationResult result =
        dataApi.create(
            dataApi
                .newRecordCreate("Movie__c")
                .setValue("Name", "Star Wars Episode V: The Empire Strikes Back")
                .setValue("Rating__c", "Excellent"));

    assertThat(result.getId(), equalTo("a00B000000FSkcvIAD"));
  }

  @Test
  public void testUpdate() throws DataApiException {
    RecordModificationResult result =
        dataApi.update(
            dataApi
                .newRecordUpdate("Movie__c", "a00B000000FSjVUIA1")
                .setValue("ReleaseDate__c", "1980-05-21"));

    assertThat(result.getId(), equalTo("a00B000000FSjVUIA1"));
  }

  @Test
  public void testUnitOfWork() throws DataApiException {
    UnitOfWork unitOfWork = dataApi.newUnitOfWork();

    ReferenceId franchiseReference =
        unitOfWork.registerCreate(
            dataApi.newRecordCreate("Franchise__c").setValue("Name", "Star Wars"));

    unitOfWork.registerCreate(
        dataApi
            .newRecordCreate("Movie__c")
            .setValue("Name", "Star Wars Episode I - A Phantom Menace")
            .setValue("Franchise__c", franchiseReference));

    unitOfWork.registerCreate(
        dataApi
            .newRecordCreate("Movie__c")
            .setValue("Name", "Star Wars Episode II - Attack Of The Clones")
            .setValue("Franchise__c", franchiseReference));

    unitOfWork.registerCreate(
        dataApi
            .newRecordCreate("Movie__c")
            .setValue("Name", "Star Wars Episode III - Revenge Of The Sith")
            .setValue("Franchise__c", franchiseReference));

    Map<ReferenceId, RecordModificationResult> results = dataApi.commitUnitOfWork(unitOfWork);

    assertThat(results, is(aMapWithSize(4)));
    assertThat(results, hasKey(equalTo(franchiseReference)));
  }

  @Test
  public void testUnitOfWorkUpdate() throws DataApiException {
    UnitOfWork unitOfWork = dataApi.newUnitOfWork();

    ReferenceId updateRecordReference =
        unitOfWork.registerUpdate(
            dataApi
                .newRecordUpdate("Movie__c", "a00B000000FSjVUIA1")
                .setValue("ReleaseDate__c", "1980-05-21"));

    Map<ReferenceId, RecordModificationResult> result = dataApi.commitUnitOfWork(unitOfWork);
    assertThat(result, is(aMapWithSize(1)));
    assertThat(result, hasKey(updateRecordReference));
  }

  @Test
  public void testGetAccessToken() {
    assertThat(
        dataApi.getAccessToken(),
        equalTo(
            "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS"));
  }
}
