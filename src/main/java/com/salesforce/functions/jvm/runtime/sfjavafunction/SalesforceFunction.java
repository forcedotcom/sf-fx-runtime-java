package com.salesforce.functions.jvm.runtime.sfjavafunction;

import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.api.ContextInvocationHandler;
import com.salesforce.functions.jvm.runtime.sfjavafunction.api.EventInvocationHandler;
import com.salesforce.functions.jvm.runtime.sfjavafunction.api.OrgContextInvocationHandler;
import com.salesforce.functions.jvm.runtime.sfjavafunction.api.UserContextInvocationHandler;
import com.salesforce.functions.jvm.runtime.sfjavafunction.cloudevent.extension.SalesforceCloudEventExtensionParser;
import com.salesforce.functions.jvm.runtime.sfjavafunction.cloudevent.extension.SalesforceContextCloudEventExtension;
import com.salesforce.functions.jvm.runtime.sfjavafunction.cloudevent.extension.SalesforceFunctionContextCloudEventExtension;
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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Wraps a user defined function. It handles all tasks that are specific to an actual invocation such as unmarshalling
 * the incoming data, marshalling the return value and parsing of {@link CloudEvent} extensions.
 *
 * Anything that can be done before an actual invocation (i.e. determining which marshallers/unmarshallers should be
 * used) is handled by {@link SalesforceFunctionsProjectFunctionsScanner}.
 */
public class SalesforceFunction implements ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceFunction.class);

    private final PayloadUnmarshaller unmarshaller;
    private final FunctionResultMarshaller marshaller;
    private final ClassLoader projectClassLoader;
    private final Object userFunctionObject;
    private final Method userFunctionApplyMethod;
    private final Class<?> eventInterfaceClass;
    private final Class<?> contextInterfaceClass;
    private final Class<?> orgContextInterfaceClass;
    private final Class<?> userContextInterfaceClass;

    public SalesforceFunction(PayloadUnmarshaller unmarshaller,
                              FunctionResultMarshaller marshaller,
                              ClassLoader projectClassLoader,
                              Object userFunctionObject,
                              Method userFunctionApplyMethod,
                              Class<?> eventInterfaceClass,
                              Class<?> contextInterfaceClass,
                              Class<?> orgContextInterfaceClass,
                              Class<?> userContextInterfaceClass
    ) {

        this.unmarshaller = unmarshaller;
        this.marshaller = marshaller;
        this.projectClassLoader = projectClassLoader;
        this.userFunctionObject = userFunctionObject;
        this.userFunctionApplyMethod = userFunctionApplyMethod;
        this.eventInterfaceClass = eventInterfaceClass;
        this.contextInterfaceClass = contextInterfaceClass;
        this.orgContextInterfaceClass = orgContextInterfaceClass;
        this.userContextInterfaceClass = userContextInterfaceClass;
    }

    @Override
    public String getName() {
        return userFunctionObject.getClass().getName();
    }

    @Override
    public SalesforceFunctionResult apply(CloudEvent cloudEvent) throws SalesforceFunctionException {
        try {
            Object payloadData = unmarshaller.unmarshall(cloudEvent);

            SalesforceContextCloudEventExtension salesforceContext =
                    SalesforceCloudEventExtensionParser.parseSalesforceContext(cloudEvent)
                            .orElseThrow(MalformedOrMissingSalesforceContextExtensionException::new);

            // Currently unused, but becomes important when we provide and API to talk to core.
            SalesforceFunctionContextCloudEventExtension salesforceFunctionContext =
                    SalesforceCloudEventExtensionParser.parseSalesforceFunctionContext(cloudEvent)
                            .orElseThrow(MalformedOrMissingSalesforceFunctionContextExtensionException::new);

            Object event = Proxy.newProxyInstance(
                    projectClassLoader,
                    new Class<?>[]{eventInterfaceClass},
                    new EventInvocationHandler(cloudEvent, payloadData)
            );

            Object userContext = Proxy.newProxyInstance(
                    projectClassLoader,
                    new Class<?>[]{userContextInterfaceClass},
                    new UserContextInvocationHandler(salesforceContext)
            );

            Object orgContext = Proxy.newProxyInstance(
                    projectClassLoader,
                    new Class<?>[]{orgContextInterfaceClass},
                    new OrgContextInvocationHandler(salesforceContext, userContext)
            );

            Object context = Proxy.newProxyInstance(
                    projectClassLoader,
                    new Class<?>[]{contextInterfaceClass},
                    new ContextInvocationHandler(cloudEvent, orgContext)
            );

            Object returnValue = userFunctionApplyMethod.invoke(userFunctionObject, event, context);
            return marshaller.marshall(returnValue);

        } catch (InvocationTargetException e) {
            throw new FunctionThrewExceptionException(e.getCause());
        } catch (IllegalAccessException e) {
            // This should never happen since the scanner will call setAccessible on the method.
            throw new SalesforceFunctionException("Unexpected IllegalAccessException while calling function", e);
        }
    }

    public PayloadUnmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    public FunctionResultMarshaller getMarshaller() {
        return marshaller;
    }
}
