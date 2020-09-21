package com.salesforce.functions.jvm.runtime.cloudevent;

import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonSerializationException;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.cloudevent.exception.*;
import com.salesforce.functions.jvm.runtime.json.*;
import com.salesforce.functions.jvm.runtime.SdkInitializer;
import io.cloudevents.CloudEvent;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

public final class SalesforceCloudEventFunction {
    private final ProjectFunction projectFunction;
    private final Class<?> cloudEventDataParameterType;
    private final Class<?> sdkParameterType;
    private final JsonLibrary returnTypeJsonLibrary;
    private final JsonLibrary cloudEventDataParameterJsonLibrary;

    public SalesforceCloudEventFunction(ProjectFunction projectFunction) throws IncompatibleFunctionException {
        this.projectFunction = projectFunction;

        if (projectFunction.isUnary()) {
            cloudEventDataParameterType = projectFunction.getParameterTypes().get(0);
            sdkParameterType = null;
        } else if (projectFunction.isBinary()){
            cloudEventDataParameterType = projectFunction.getParameterTypes().get(0);
            sdkParameterType = projectFunction.getParameterTypes().get(1);

            if (!SdkInitializer.canInitializeClass(sdkParameterType)) {
                throw new IncompatibleFunctionException("Second function parameter must be a compatible SDK type!");
            }
        } else {
            throw new IncompatibleFunctionException("Function must be either unary or binary!");
        }

        try {
            cloudEventDataParameterJsonLibrary = JsonLibraryDetector.detect(cloudEventDataParameterType);
        } catch (AmbiguousJsonLibraryException e) {
            throw new IncompatibleFunctionException("Cannot determine JSON library for first function parameter!", e);
        }

        try {
            returnTypeJsonLibrary = JsonLibraryDetector.detect(projectFunction.getReturnType());
        } catch (AmbiguousJsonLibraryException e) {
            throw new IncompatibleFunctionException("Cannot determine JSON library for function return value!", e);
        }
    }

    public String apply(CloudEvent cloudEvent) throws SalesforceCloudEventFunctionException {
        if (!cloudEvent.getType().equals(SF_FUNCTION_SYNC_INVOKE_CLOUD_EVENT_TYPE) &&
                !cloudEvent.getType().equals(EVERGREEN_TEST_CLOUD_EVENT_TYPE)) {
            throw new IncompatibleCloudEventTypeException();
        }

        final String dataContentType = cloudEvent.getDataContentType();
        if (dataContentType == null || !dataContentType.startsWith("application/json")) {
            throw new IncompatibleCloudEventDataContentTypeException();
        }

        final byte[] cloudEventData = cloudEvent.getData();
        // Nullary functions are currently not supported.
        if (cloudEventData == null || cloudEventData.length == 0) {
            throw new MissingCloudEventDataException();
        }

        // We want to ensure the extensions are present and parsable or fail early
        SalesforceContextParser
                .parseSalesforceContext(cloudEvent)
                .orElseThrow(MalformedOrMissingSalesforceContextExtensionException::new);

        SalesforceContextParser
                .parseSalesforceFunctionContext(cloudEvent)
                .orElseThrow(MalformedOrMissingSalesforceFunctionContextExtensionException::new);

        final Object cloudEventDataParameter;
        try {
            // RFC 4627 and RFC 7159 both state that the default encoding for JSON is UTF-8. RFC 7159 even goes so far
            // to encourage implementations to only use UTF-8 for maximum interoperability. We only support UTF-8 here
            // for the same reasons since, especially when dealing with RFC 7159 JSON, detecting the charset is very
            // hard. Please note that the JSON mime-type does not allow for a charset attribute that we can use.
            //
            // Invalid UTF-8 is not a concern here, when Java's parser detects invalid data, it will insert Unicode's
            // replacement character and continue to parse the data. There won't be an exception but actual JSON parsing
            // will most likely fail later on.
            String cloudEventDataUtf8String = new String(cloudEventData, StandardCharsets.UTF_8);
            cloudEventDataParameter = cloudEventDataParameterJsonLibrary.deserializeAt(cloudEventDataUtf8String, cloudEventDataParameterType);
        } catch (JsonDeserializationException e) {
            throw new FunctionParameterJsonDeserializationException(e);
        }

        try {
            final Object functionResult;
            if (projectFunction.isBinary()) {
                Object sdk = SdkInitializer
                        .initialize(sdkParameterType, cloudEvent)
                        .orElseThrow(SdkInitializationException::new);

                functionResult = projectFunction.apply(cloudEventDataParameter, sdk);
            } else {
                functionResult = projectFunction.apply(cloudEventDataParameter);
            }

            return returnTypeJsonLibrary.serialize(functionResult);
        } catch (InvocationTargetException e) {
            throw new FunctionThrewExceptionException(e.getCause());
        } catch (IllegalAccessException | JsonSerializationException e) {
            throw new SalesforceCloudEventFunctionException(e);
        }
    }

    private static final String SF_FUNCTION_SYNC_INVOKE_CLOUD_EVENT_TYPE = "com.salesforce.function.invoke.sync";
    private static final String EVERGREEN_TEST_CLOUD_EVENT_TYPE = "com.evergreen.functions.test";
}
