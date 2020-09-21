package com.salesforce.functions.jvm.runtime;


import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContext;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceContextParser;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceFunctionContext;
import io.cloudevents.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public final class SdkInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SdkInitializer.class);

    public static boolean canInitializeClass(Class<?> clazz) {
        if (clazz.getCanonicalName().equals("com.salesforce.functions.jvm.placeholder.sdk.Context")) {
            try {
                clazz.getDeclaredMethod("initializeFromRuntime", String.class, String.class, String.class);
            } catch (NoSuchMethodException e) {
                LOGGER.debug("Could not find initialization method of placeholder SDK!", e);
                return false;
            }

            return true;
        }

        return false;
    }

    public static Optional<Object> initialize(Class<?> clazz, CloudEvent cloudEvent) {
        if (clazz.getCanonicalName().equals("com.salesforce.functions.jvm.placeholder.sdk.Context")) {
            try {
                Method initializeFromRuntimeMethod = clazz.getDeclaredMethod("initializeFromRuntime", String.class, String.class, String.class);
                initializeFromRuntimeMethod.setAccessible(true);

                SalesforceContextParser.parseSalesforceContext(cloudEvent);

                Optional<String> optionalAccessToken = SalesforceContextParser
                        .parseSalesforceFunctionContext(cloudEvent)
                        .map(SalesforceFunctionContext::getAccessToken);

                Optional<SalesforceContext> optionalSalesforceContext = SalesforceContextParser
                        .parseSalesforceContext(cloudEvent);

                if (optionalAccessToken.isPresent() && optionalSalesforceContext.isPresent()) {
                    String salesforceBaseUrl = optionalSalesforceContext.get().getUserContext().getSalesforceBaseUrl();
                    String apiVersion = optionalSalesforceContext.get().getApiVersion();
                    String accessToken = optionalAccessToken.get();

                    return Optional.of(initializeFromRuntimeMethod.invoke(null, salesforceBaseUrl, apiVersion, accessToken));
                } else {
                    LOGGER.debug("Could not parse required information for SDK ({}) initialization from CloudEvent!", clazz.getCanonicalName());
                    return Optional.empty();
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("Unexpected exception while initializing SDK ({})", clazz.getCanonicalName(), e);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
