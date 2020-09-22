package com.salesforce.functions.jvm.runtime.cloudevent;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class SalesforceContextParserTest {

    @Test
    public void testParseSalesforceContext() {
        Optional<SalesforceContext> salesforceContext = SalesforceContextParser.parseSalesforceContext(BASE_TESTING_CLOUD_EVENT);

        assertEquals(Optional.empty(), salesforceContext);
    }

    @Test
    public void testParseSalesforceContextWithCapturedCoreInvocationData() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withExtension("sfcontext", "eyJhcGlWZXJzaW9uIjoiNTAuMCIsInBheWxvYWRWZXJzaW9uIjoiMC4xIiwidXNlckNvbnRleHQiOnsib3JnSWQiOiIwMER4eDAwMDAwMDZJWUoiLCJ1c2VySWQiOiIwMDV4eDAwMDAwMVg4VXoiLCJvbkJlaGFsZk9mVXNlcklkIjpudWxsLCJ1c2VybmFtZSI6InRlc3QtenFpc25mNnl0bHF2QGV4YW1wbGUuY29tIiwic2FsZXNmb3JjZUJhc2VVcmwiOiJodHRwOi8vcGlzdGFjaGlvLXZpcmdvLTEwNjMtZGV2LWVkLmxvY2FsaG9zdC5pbnRlcm5hbC5zYWxlc2ZvcmNlLmNvbTo2MTA5Iiwib3JnRG9tYWluVXJsIjoiaHR0cDovL3Bpc3RhY2hpby12aXJnby0xMDYzLWRldi1lZC5sb2NhbGhvc3QuaW50ZXJuYWwuc2FsZXNmb3JjZS5jb206NjEwOSJ9fQ==")
                .build();

        SalesforceContext.UserContext userContext = new SalesforceContext.UserContext(
                "00Dxx0000006IYJ",
                "005xx000001X8Uz",
                null,
                "test-zqisnf6ytlqv@example.com",
                "http://pistachio-virgo-1063-dev-ed.localhost.internal.salesforce.com:6109",
                "http://pistachio-virgo-1063-dev-ed.localhost.internal.salesforce.com:6109"
        );

        SalesforceContext salesforceContext = new SalesforceContext("50.0", "0.1", userContext);

        Optional<SalesforceContext> result = SalesforceContextParser.parseSalesforceContext(cloudEvent);

        assertTrue(result.isPresent());
        assertEquals(salesforceContext, result.get());
    }

    @Test
    public void testParseSalesforceFunctionContextWithCapturedCoreInvocationData() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withExtension("sffncontext", "eyJhY2Nlc3NUb2tlbiI6IjAwRHh4MDAwMDAwNklZSiFBUUVBUU5SYWM1YTFoUmhoZjAySFJlZ3c0c1NadktoOW9ZLm9oZFFfYV9LNHg1ZHdBZEdlZ1dlbVhWNnBOVVZLaFpfdVkyOUZ4SUVGTE9adTBHZjlvZk1HVzBIRkxacDgiLCJmdW5jdGlvbkludm9jYXRpb25JZCI6bnVsbCwiZnVuY3Rpb25OYW1lIjoiTXlGdW5jdGlvbiIsImFwZXhDbGFzc0lkIjpudWxsLCJhcGV4Q2xhc3NGUU4iOm51bGwsInJlcXVlc3RJZCI6IjAwRHh4MDAwMDAwNklZSkVBMi00WTRXM0x3X0xrb3NrY0hkRWFaemUtLU15RnVuY3Rpb24tMjAyMC0wOS0wM1QyMDo1NjoyNy42MDg0NDRaIiwicmVzb3VyY2UiOiJodHRwOi8vZGhhZ2Jlcmctd3NsMTo4MDgwIn0")
                .build();

        SalesforceFunctionContext salesforceFunctionContext = new SalesforceFunctionContext(
                "00Dxx0000006IYJ!AQEAQNRac5a1hRhhf02HRegw4sSZvKh9oY.ohdQ_a_K4x5dwAdGegWemXV6pNUVKhZ_uY29FxIEFLOZu0Gf9ofMGW0HFLZp8",
                null,
                "MyFunction",
                null,
                null,
                "00Dxx0000006IYJEA2-4Y4W3Lw_LkoskcHdEaZze--MyFunction-2020-09-03T20:56:27.608444Z",
                "http://dhagberg-wsl1:8080"
        );

        Optional<SalesforceFunctionContext> result = SalesforceContextParser.parseSalesforceFunctionContext(cloudEvent);

        assertTrue(result.isPresent());
        assertEquals(salesforceFunctionContext, result.get());
    }

    @Test
    public void testParseSalesforceFunctionContextWithInvalidJson() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withExtension("sffncontext", Base64.getEncoder().encodeToString("{".getBytes(StandardCharsets.UTF_8)))
                .build();

        Optional<SalesforceFunctionContext> result = SalesforceContextParser.parseSalesforceFunctionContext(cloudEvent);

        assertFalse(result.isPresent());
    }

    @Test
    public void testParseSalesforceContextWithInvalidJson() {
        CloudEvent cloudEvent = CloudEventBuilder
                .v1(BASE_TESTING_CLOUD_EVENT)
                .withExtension("sfcontext", Base64.getEncoder().encodeToString("{".getBytes(StandardCharsets.UTF_8)))
                .build();

        Optional<SalesforceContext> result = SalesforceContextParser.parseSalesforceContext(cloudEvent);

        assertFalse(result.isPresent());
    }

    private CloudEvent BASE_TESTING_CLOUD_EVENT = CloudEventBuilder
            .v1()
            .withId("fe9da89b-1eed-471c-a04c-0b3c664b63af")
            .withSource(URI.create("urn:sf-fx-runtime-java:testing"))
            .withType("com.salesforce.function.invoke.sync")
            .build();
}
