/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.UserContext;
import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.User;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;

public class ContextImplTest {
  private CloudEvent cloudEvent;
  private SalesforceContextCloudEventExtension contextExtension;
  private SalesforceFunctionContextCloudEventExtension functionContextExtension;

  @Before
  public void setUp() {
    this.cloudEvent =
        new CloudEventBuilder()
            .withId("id-1234-abc")
            .withSource(URI.create("urn:source"))
            .withType("type")
            .build();

    this.contextExtension =
        new SalesforceContextCloudEventExtension(
            "53.0",
            "xyz",
            new UserContext(
                "00Dxx0000006IYJ",
                "005xx000001X8Uz",
                null,
                "test-zqisnf6ytlqv@example.com",
                URI.create("https://example.com/salesforceBaseUrl"),
                URI.create("https://example.com/orgDomainUrl")));

    this.functionContextExtension =
        new SalesforceFunctionContextCloudEventExtension(
            "00Dxx0000006IYJ!AQEAQNRac5a1hRhhf02HRegw4sSZvKh9oY.ohdQ_a_K4x5dwAdGegWemXV6pNUVKhZ_uY29FxIEFLOZu0Gf9ofMGW0HFLZp8",
            "invocation-id-123-abc",
            "example-function",
            null,
            null,
            "request-id",
            "resource");
  }

  @Test
  public void testContextValues() {
    Context context = new ContextImpl(cloudEvent, contextExtension, functionContextExtension);

    assertThat(context.getId(), is(equalTo(cloudEvent.getId())));
    assertThat(context.getOrg(), is(optionalWithValue()));
  }

  @Test
  public void testOrgValues() {
    Org org = new OrgImpl(contextExtension, functionContextExtension);

    assertThat(org.getId(), is(equalTo(contextExtension.getUserContext().getOrgId())));
    assertThat(
        org.getBaseUrl(), is(equalTo(contextExtension.getUserContext().getSalesforceBaseUrl())));
    assertThat(
        org.getDomainUrl(), is(equalTo(contextExtension.getUserContext().getOrgDomainUrl())));
    assertThat(org.getApiVersion(), is(equalTo(contextExtension.getApiVersion())));
    assertThat(org.getUser(), is(notNullValue()));
    assertThat(org.getDataApi(), is(notNullValue()));
  }

  @Test
  public void testUserValues() {
    User user = new UserImpl(contextExtension);

    assertThat(user.getId(), is(equalTo(contextExtension.getUserContext().getUserId())));
    assertThat(user.getUsername(), is(equalTo(contextExtension.getUserContext().getUsername())));
    assertThat(user.getOnBehalfOfUserId(), is(emptyOptional()));
  }
}
