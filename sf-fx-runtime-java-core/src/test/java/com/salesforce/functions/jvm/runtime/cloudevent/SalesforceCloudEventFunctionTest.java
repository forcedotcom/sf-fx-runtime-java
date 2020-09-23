package com.salesforce.functions.jvm.runtime.cloudevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.cloudevent.exception.*;
import com.salesforce.functions.jvm.runtime.testing.ProjectFunctionTestingHelper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SalesforceCloudEventFunctionTest {
    private Gson gson = new Gson();
    private SalesforceCloudEventFunction toUpperCaseFunction =
            new SalesforceCloudEventFunction(ProjectFunctionTestingHelper.createProjectFunction(String::toUpperCase, String.class, String.class));

    @Test
    public void testHappyPath() {
        String result = toUpperCaseFunction.apply(BASE_TESTING_CLOUD_EVENT);
        assertEquals("\"TEST MESSAGE\"", result);
    }

    @Test(expected = IncompatibleFunctionException.class)
    public void testIncompatibleFunction() {
        new SalesforceCloudEventFunction(ProjectFunctionTestingHelper.createProjectFunction((a, b) -> a + b, String.class, String.class, String.class));
    }


    @Test(expected = IncompatibleFunctionException.class)
    public void testIncompatibleFunctionDueToAmbiguousJsonLibrary() {
        class Foo {
            @JsonProperty("datax")
            @SerializedName("datax")
            private String data;
        }

        new SalesforceCloudEventFunction(ProjectFunctionTestingHelper.createProjectFunction((a) -> "constant", Foo.class, String.class));
    }

    @Test(expected = IncompatibleFunctionException.class)
    public void testIncompatibleFunctionDueToAmbiguousJsonLibrary2() {
        class Foo {
            @JsonProperty("datax")
            @SerializedName("datax")
            private String data;
        }

        new SalesforceCloudEventFunction(ProjectFunctionTestingHelper.createProjectFunction((a) -> new Foo(), String.class, Foo.class));
    }

    @Test
    public void testLegacyEvergreenCloudEventType() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withType("com.evergreen.functions.test")
                .build();

        toUpperCaseFunction.apply(cloudEvent);
    }

    @Test(expected = IncompatibleCloudEventTypeException.class)
    public void testIncompatibleCloudEventType() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withType("com.salesforce.function.invoke.async")
                .build();

        toUpperCaseFunction.apply(cloudEvent);
    }

    @Test(expected = IncompatibleCloudEventDataContentTypeException.class)
    public void testIncompatibleCloudEventDataContentType() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withDataContentType("text/plain")
                .build();

        toUpperCaseFunction.apply(cloudEvent);
    }

    @Test(expected = MalformedOrMissingSalesforceContextExtensionException.class)
    public void testMissingSalesforceContextExtension() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withoutExtension("sfcontext")
                .build();

        toUpperCaseFunction.apply(cloudEvent);
    }

    @Test(expected = MalformedOrMissingSalesforceFunctionContextExtensionException.class)
    public void testMissingSalesforceFunctionContextExtension() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withoutExtension("sffncontext")
                .build();

        toUpperCaseFunction.apply(cloudEvent);
    }

    @Test(expected = MissingCloudEventDataException.class)
    public void testMissingCloudEventData() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withData("application/json", "".getBytes(StandardCharsets.UTF_8))
                .build();

        toUpperCaseFunction.apply(cloudEvent);
    }

    @Test(expected = FunctionParameterJsonDeserializationException.class)
    public void testInvalidFunctionParameterJson() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withData("application/json", "[".getBytes(StandardCharsets.UTF_8))
                .build();

        toUpperCaseFunction.apply(cloudEvent);
    }

    private SalesforceContext salesforceContext = new SalesforceContext(
            "50.0",
            "0.1",
            new SalesforceContext.UserContext(
                    "orgid",
                    "userid",
                    null,
                    "username",
                    "https://example.com/base",
                    "https://example.com/orgDomain"
            )
    );

    private SalesforceFunctionContext salesforceFunctionContext = new SalesforceFunctionContext(
            "accesstoken",
            "funcinvocid",
            "functionname",
            null,
            null,
            "request-id",
            "resource"
    );

    private CloudEvent BASE_TESTING_CLOUD_EVENT = CloudEventBuilder
            .v1()
            .withId("fe9da89b-1eed-471c-a04c-0b3c664b63af")
            .withSource(URI.create("urn:sf-fx-runtime-java:testing"))
            .withType("com.salesforce.function.invoke.sync")
            .withExtension("sfcontext", Base64.getEncoder().encodeToString(gson.toJson(salesforceContext).getBytes(StandardCharsets.UTF_8)))
            .withExtension("sffncontext", Base64.getEncoder().encodeToString(gson.toJson(salesforceFunctionContext).getBytes(StandardCharsets.UTF_8)))
            .withData("application/json", "\"test message\"".getBytes(StandardCharsets.UTF_8))
            .build();
}
