/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sfjavafunction;

import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceCloudEventExtensionParser;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionThrewExceptionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.MalformedOrMissingSalesforceContextExtensionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.MalformedOrMissingSalesforceFunctionContextExtensionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.SalesforceFunctionException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.FunctionResultMarshaller;
import com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling.PayloadUnmarshaller;
import io.cloudevents.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Wraps a user defined function. It handles all tasks that are specific to an actual invocation such as unmarshalling
 * the incoming data, marshalling the return value and parsing of {@link CloudEvent} extensions.
 * <p>
 * Anything that can be done before an actual invocation (i.e. determining which marshallers/unmarshallers should be
 * used) is handled by {@link SalesforceFunctionsProjectFunctionsScanner}.
 */
public class SalesforceFunction implements ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceFunction.class);

    private final PayloadUnmarshaller unmarshaller;
    private final FunctionResultMarshaller marshaller;
    private final InvocationWrapper functionThing;

    public SalesforceFunction(PayloadUnmarshaller unmarshaller,
                              FunctionResultMarshaller marshaller,
                              InvocationWrapper functionThing
    ) {
        this.unmarshaller = unmarshaller;
        this.marshaller = marshaller;
        this.functionThing = functionThing;
    }

    @Override
    public String getName() {
        return "UNIMPLEMENTED";
    }

    @Override
    public SalesforceFunctionResult apply(CloudEvent cloudEvent) throws SalesforceFunctionException {
        try {
            SalesforceContextCloudEventExtension salesforceContext =
                    SalesforceCloudEventExtensionParser.parseSalesforceContext(cloudEvent)
                            .orElseThrow(MalformedOrMissingSalesforceContextExtensionException::new);

            SalesforceFunctionContextCloudEventExtension salesforceFunctionContext =
                    SalesforceCloudEventExtensionParser.parseSalesforceFunctionContext(cloudEvent)
                            .orElseThrow(MalformedOrMissingSalesforceFunctionContextExtensionException::new);

            Object payloadData = unmarshaller.unmarshall(cloudEvent);
            Object returnValue = functionThing.invoke(payloadData, cloudEvent, salesforceContext, salesforceFunctionContext);
            return marshaller.marshall(returnValue);

        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
            throw new FunctionThrewExceptionException(e.getCause());
        } catch (IllegalAccessException e) {
            // This should never happen since the scanner will call setAccessible on the method.
            throw new SalesforceFunctionException("Unexpected IllegalAccessException while calling function", e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public PayloadUnmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    public FunctionResultMarshaller getMarshaller() {
        return marshaller;
    }
}
