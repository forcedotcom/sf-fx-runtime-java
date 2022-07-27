/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalToIgnoringCase;

import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.ReferenceId;
import com.salesforce.functions.jvm.sdk.data.builder.RecordBuilder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import org.junit.Test;
import org.mockito.Mockito;

public class RecordBuilderImplTest {
  private final DataApiImpl dataApi =
      new DataApiImpl(
          URI.create("http://localhost:8080/"),
          "53.0",
          "00DB0000000UIn2!AQMAQKXBvR03lDdfMiD6Pdpo_wiMs6LGp6dVkrwOuqiiTEmwdPb8MvSZwdPLe009qHlwjxIVa4gY.JSAd0mfgRRz22vS");

  @Test
  public void testEmptyRecord() {
    RecordBuilder recordBuilder = dataApi.newRecordBuilder("Movie__c");
    Record record = recordBuilder.build();
    assertThat(record.getFieldNames(), is(empty()));
  }

  @Test
  public void testFieldNameCaseSensitivity() {
    final String movieName = "Star Wars Episode IV - A New Hope";

    RecordBuilder recordBuilder = dataApi.newRecordBuilder("Movie__c");
    recordBuilder.withField("Name", movieName);
    Record record = recordBuilder.build();

    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("name")));

    assertThat(record.hasField("Name"), is(true));
    assertThat(record.hasField("name"), is(true));
    assertThat(record.hasField("naME"), is(true));

    assertThat(record.getStringField("Name"), is(optionalWithValue(equalTo(movieName))));
    assertThat(record.getStringField("name"), is(optionalWithValue(equalTo(movieName))));
    assertThat(record.getStringField("naME"), is(optionalWithValue(equalTo(movieName))));
  }

  @Test
  public void testFieldNameCaseSensitivityWhenOverwritten() {
    final String movieName1 = "Star Wars Episode IV - A New Hope";
    final String movieName2 = "Star Wars Episode V - The Empire Strikes Back";

    RecordBuilder recordBuilder = dataApi.newRecordBuilder("Movie__c");
    recordBuilder.withField("Name", movieName1);
    recordBuilder.withField("NAME", movieName2);
    Record record = recordBuilder.build();

    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("name")));

    assertThat(record.hasField("Name"), is(true));
    assertThat(record.hasField("NAME"), is(true));
    assertThat(record.hasField("name"), is(true));
    assertThat(record.hasField("naME"), is(true));

    assertThat(record.getStringField("Name"), is(optionalWithValue(equalTo(movieName2))));
    assertThat(record.getStringField("NAME"), is(optionalWithValue(equalTo(movieName2))));
    assertThat(record.getStringField("name"), is(optionalWithValue(equalTo(movieName2))));
    assertThat(record.getStringField("naME"), is(optionalWithValue(equalTo(movieName2))));
  }

  @Test
  public void testWithoutField() {
    RecordBuilder recordBuilder =
        dataApi
            .newRecordBuilder("Movie__c")
            .withField("Name", "Star Wars")
            .withField("Flavor__c", "Vanilla");

    assertThat(
        recordBuilder.getStringField("Flavor__c"), is(optionalWithValue(equalTo("Vanilla"))));

    recordBuilder.withoutField("Flavor__c");

    assertThat(recordBuilder.getStringField("Flavor__c"), is(emptyOptional()));
    assertThat(recordBuilder.isNullField("Flavor__c"), is(false));

    Record record = recordBuilder.build();
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Name")));
  }

  @Test
  public void testWithNullField() {
    Record record = dataApi.newRecordBuilder("Movie__c").withNullField("Rating__c").build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test
  public void testWithFieldStringNull() {
    Record record =
        dataApi.newRecordBuilder("Movie__c").withField("Rating__c", (String) null).build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test
  public void testWithFieldBigDecimalNull() {
    Record record =
        dataApi.newRecordBuilder("Movie__c").withField("Rating__c", (BigDecimal) null).build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test
  public void testWithFieldBigIntegerNull() {
    Record record =
        dataApi.newRecordBuilder("Movie__c").withField("Rating__c", (BigInteger) null).build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test
  public void testWithFieldReferenceIdNull() {
    Record record =
        dataApi.newRecordBuilder("Movie__c").withField("Rating__c", (ReferenceId) null).build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testForeignReferenceId() {
    RecordBuilder recordBuilder = dataApi.newRecordBuilder("Movie__c");
    recordBuilder.withField("Franchise__c", Mockito.mock(ReferenceId.class));
  }
}
