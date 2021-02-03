/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sfjavafunction;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import io.cloudevents.CloudEvent;

import java.lang.reflect.InvocationTargetException;

public interface InvocationWrapper {
    Object invoke(
            Object payload,
            CloudEvent cloudEvent,
            SalesforceContextCloudEventExtension salesforceContext,
            SalesforceFunctionContextCloudEventExtension functionContext
    ) throws
            InvocationTargetException,
            IllegalAccessException,
            InstantiationException;
}
