/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import io.cloudevents.CloudEvent;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

public class Slf4jMdcDataInvocationWrapperTest {
  @Before
  public void beforeEach() {
    MDC.clear();
  }

  @Test
  public void testForWhenSlf4jIsAvailable() {
    InvocationWrapper mockedInvocationWrapper = mock(InvocationWrapper.class);

    Slf4j1MdcDataInvocationWrapper wrapper =
        new Slf4j1MdcDataInvocationWrapper(getClass().getClassLoader(), mockedInvocationWrapper);

    String cloudEventId = UUID.randomUUID().toString();

    CloudEvent cloudEvent = mock(CloudEvent.class);
    when(cloudEvent.getId()).thenReturn(cloudEventId);

    SalesforceContextCloudEventExtension contextExtension =
        mock(SalesforceContextCloudEventExtension.class);

    SalesforceFunctionContextCloudEventExtension functionExtension =
        mock(SalesforceFunctionContextCloudEventExtension.class);

    Object payload = "payload";

    wrapper.invoke(payload, cloudEvent, contextExtension, functionExtension);

    assertThat(MDC.get("function-invocation-id"), is(equalTo(cloudEventId)));

    verify(mockedInvocationWrapper)
        .invoke(payload, cloudEvent, contextExtension, functionExtension);
  }

  @Test
  public void testWhenSlf4jIsUnavailable() {
    InvocationWrapper mockedInvocationWrapper = mock(InvocationWrapper.class);

    ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();

    Slf4j1MdcDataInvocationWrapper wrapper =
        new Slf4j1MdcDataInvocationWrapper(bootstrapClassLoader, mockedInvocationWrapper);

    String cloudEventId = UUID.randomUUID().toString();

    CloudEvent cloudEvent = mock(CloudEvent.class);
    when(cloudEvent.getId()).thenReturn(cloudEventId);

    SalesforceContextCloudEventExtension contextExtension =
        mock(SalesforceContextCloudEventExtension.class);

    SalesforceFunctionContextCloudEventExtension functionExtension =
        mock(SalesforceFunctionContextCloudEventExtension.class);

    Object payload = "payload";

    wrapper.invoke(payload, cloudEvent, contextExtension, functionExtension);

    assertThat(MDC.get("function-invocation-id"), is(nullValue()));

    verify(mockedInvocationWrapper)
        .invoke(payload, cloudEvent, contextExtension, functionExtension);
  }
}
