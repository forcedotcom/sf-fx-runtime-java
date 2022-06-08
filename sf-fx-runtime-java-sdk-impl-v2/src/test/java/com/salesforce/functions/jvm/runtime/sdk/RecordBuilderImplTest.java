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

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.UserContext;
import com.salesforce.functions.jvm.sdk.Record;
import com.salesforce.functions.jvm.sdk.RecordBuilder;
import com.salesforce.functions.jvm.sdk.ReferenceId;
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

  private final UserContext userContext =
      new UserContext(
          "00Dxx0000006IYJ",
          "005xx000001X8Uz",
          null,
          "test-zqisnf6ytlqv@example.com",
          URI.create("http://pistachio-virgo-1063-dev-ed.localhost.internal.salesforce.com:6109"),
          URI.create("http://pistachio-virgo-1063-dev-ed.localhost.internal.salesforce.com:6109"));

  private final SalesforceContextCloudEventExtension salesforceContext =
      new SalesforceContextCloudEventExtension("50.0", "0.1", userContext);

  private final SalesforceFunctionContextCloudEventExtension salesforceFunctionContext =
      new SalesforceFunctionContextCloudEventExtension(
          "00Dxx0000006IYJ!AQEAQNRac5a1hRhhf02HRegw4sSZvKh9oY.ohdQ_a_K4x5dwAdGegWemXV6pNUVKhZ_uY29FxIEFLOZu0Gf9ofMGW0HFLZp8",
          null,
          "MyFunction",
          null,
          null,
          "00Dxx0000006IYJEA2-4Y4W3Lw_LkoskcHdEaZze--MyFunction-2020-09-03T20:56:27.608444Z",
          "http://dhagberg-wsl1:8080");

  private final OrgImpl orgimpl = new OrgImpl(salesforceContext, salesforceFunctionContext, "53.0");

  @Test
  public void testEmptyRecord() {
    Record record = orgimpl.newRecordBuilder("Movie__c").build();

    assertThat(record.getFieldNames(), is(empty()));
  }

  @Test
  public void testFieldNameCaseSensitivity() {
    final String movieName = "Star Wars Episode IV - A New Hope";

    RecordBuilder recordBuilder = orgimpl.newRecordBuilder("Movie__c");
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

    RecordBuilder recordBuilder = orgimpl.newRecordBuilder("Movie__c");
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
        orgimpl
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
    Record record = orgimpl.newRecordBuilder("Movie__c").withNullField("Rating__c").build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test
  public void testWithFieldStringNull() {
    Record record =
        orgimpl.newRecordBuilder("Movie__c").withField("Rating__c", (String) null).build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test
  public void testWithFieldBigDecimalNull() {
    Record record =
        orgimpl.newRecordBuilder("Movie__c").withField("Rating__c", (BigDecimal) null).build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test
  public void testWithFieldBigIntegerNull() {
    Record record =
        orgimpl.newRecordBuilder("Movie__c").withField("Rating__c", (BigInteger) null).build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test
  public void testWithFieldReferenceIdNull() {
    Record record =
        orgimpl.newRecordBuilder("Movie__c").withField("Rating__c", (ReferenceId) null).build();

    assertThat(record.isNullField("Rating__c"), is(true));
    assertThat(record.getFieldNames(), contains(equalToIgnoringCase("Rating__c")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testForeignReferenceId() {
    RecordBuilder recordBuilder = orgimpl.newRecordBuilder("Movie__c");
    recordBuilder.withField("Franchise__c", Mockito.mock(ReferenceId.class));
  }
}
