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
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.salesforce.functions.jvm.sdk.data.*;
import com.salesforce.functions.jvm.sdk.data.builder.UnitOfWorkBuilder;
import com.salesforce.functions.jvm.sdk.data.error.DataApiException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

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
        records.get(0).getStringField("Name"),
        is(optionalWithValue(equalTo("An awesome test account"))));

    assertThat(
        records.get(1).getStringField("Name"), is(optionalWithValue(equalTo("Global Media"))));

    assertThat(records.get(2).getStringField("Name"), is(optionalWithValue(equalTo("Acme"))));

    assertThat(
        records.get(3).getStringField("Name"), is(optionalWithValue(equalTo("salesforce.com"))));

    assertThat(
        records.get(4).getStringField("Name"),
        is(optionalWithValue(equalTo("Sample Account for Entitlements"))));
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
      assertThat(e.getDataApiErrors().get(0).getErrorCode(), is(equalTo("MALFORMED_QUERY")));
      assertThat(e.getDataApiErrors().get(0).getMessage(), is(equalTo("unexpected token: SELEKT")));
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
                .newRecordBuilder("Movie__c")
                .withField("Name", "Star Wars Episode V: The Empire Strikes Back")
                .withField("Rating__c", "Excellent")
                .build());

    assertThat(result.getId(), is(equalTo("a00B000000FSkcvIAD")));
  }

  @Test
  public void testUpdate() throws DataApiException {
    RecordModificationResult result =
        dataApi.update(
            dataApi
                .newRecordBuilder("Movie__c")
                .withField("Id", "a00B000000FSjVUIA1")
                .withField("ReleaseDate__c", "1980-05-21")
                .build());

    assertThat(result.getId(), is(equalTo("a00B000000FSjVUIA1")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateWithoutIdField() throws DataApiException {
    dataApi.update(
        dataApi.newRecordBuilder("Movie__c").withField("ReleaseDate__c", "1980-05-21").build());
  }

  @Test
  public void testDelete() throws DataApiException {
    RecordModificationResult result = dataApi.delete("Account", "001B000001Lp1FxIAJ");

    assertThat(result.getId(), is(equalTo("001B000001Lp1FxIAJ")));
  }

  @Test
  public void testUnitOfWork() throws DataApiException {
    UnitOfWorkBuilder unitOfWorkBuilder = dataApi.newUnitOfWorkBuilder();

    ReferenceId franchiseReference =
        unitOfWorkBuilder.registerCreate(
            dataApi.newRecordBuilder("Franchise__c").withField("Name", "Star Wars").build());

    unitOfWorkBuilder.registerCreate(
        dataApi
            .newRecordBuilder("Movie__c")
            .withField("Name", "Star Wars Episode I - A Phantom Menace")
            .withField("Franchise__c", franchiseReference)
            .build());

    unitOfWorkBuilder.registerCreate(
        dataApi
            .newRecordBuilder("Movie__c")
            .withField("Name", "Star Wars Episode II - Attack Of The Clones")
            .withField("Franchise__c", franchiseReference)
            .build());

    unitOfWorkBuilder.registerCreate(
        dataApi
            .newRecordBuilder("Movie__c")
            .withField("Name", "Star Wars Episode III - Revenge Of The Sith")
            .withField("Franchise__c", franchiseReference)
            .build());

    Map<ReferenceId, RecordModificationResult> results =
        dataApi.commitUnitOfWork(unitOfWorkBuilder.build());

    assertThat(results, is(aMapWithSize(4)));
    assertThat(results, hasKey(equalTo(franchiseReference)));
  }

  @Test
  public void testUnitOfWorkUpdate() throws DataApiException {
    UnitOfWorkBuilder unitOfWorkBuilder = dataApi.newUnitOfWorkBuilder();

    ReferenceId updateRecordReference =
        unitOfWorkBuilder.registerUpdate(
            dataApi
                .newRecordBuilder("Movie__c")
                .withField("Id", "a01B0000009gSrFIAU")
                .withField("ReleaseDate__c", "1980-05-21")
                .build());

    Map<ReferenceId, RecordModificationResult> result =
        dataApi.commitUnitOfWork(unitOfWorkBuilder.build());

    assertThat(result, is(aMapWithSize(1)));
    assertThat(result, hasKey(updateRecordReference));
  }

  @Test
  public void testUnitOfWorkDelete() throws DataApiException {
    UnitOfWorkBuilder unitOfWorkBuilder = dataApi.newUnitOfWorkBuilder();

    ReferenceId deleteRecordReference =
        unitOfWorkBuilder.registerDelete("Movie__c", "a01B0000009gSr9IAE");

    Map<ReferenceId, RecordModificationResult> result =
        dataApi.commitUnitOfWork(unitOfWorkBuilder.build());

    assertThat(result, is(aMapWithSize(1)));
    assertThat(result, hasKey(deleteRecordReference));
    assertThat(result.get(deleteRecordReference).getId(), is(equalTo("a01B0000009gSr9IAE")));
  }

  @Test
  public void testNewRecordBuilderFromRecord() {
    Record record =
        dataApi
            .newRecordBuilder("Movie__c")
            .withField("Name", "Star Wars")
            .withField("Rating__c", "Excellent")
            .build();

    Record record2 =
        dataApi
            .newRecordBuilder(record)
            .withField("Name", "Star Wars Episode VI - Return Of The Jedi")
            .build();

    assertThat(record2.getType(), is(equalTo("Movie__c")));

    assertThat(
        record2.getStringField("Rating__c"), is(equalTo(record.getStringField("Rating__c"))));

    assertThat(
        record2.getStringField("Name"),
        is(optionalWithValue(equalTo("Star Wars Episode VI - Return Of The Jedi"))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommitUnitOfWorkWithForeignUnitOfWork() throws DataApiException {
    dataApi.commitUnitOfWork(Mockito.mock(UnitOfWork.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateWithForeignRecord() throws DataApiException {
    dataApi.create(Mockito.mock(Record.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateWithForeignRecord() throws DataApiException {
    dataApi.update(Mockito.mock(Record.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewRecordBuilderWithForeignRecord() {
    dataApi.newRecordBuilder(Mockito.mock(Record.class));
  }

  @Test
  public void testGetAccessToken() {
    assertThat(
        dataApi.getAccessToken(),
        equalTo(
            "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS"));
  }
}
