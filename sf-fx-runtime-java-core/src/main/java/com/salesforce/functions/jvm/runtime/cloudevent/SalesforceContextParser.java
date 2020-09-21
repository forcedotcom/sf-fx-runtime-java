package com.salesforce.functions.jvm.runtime.cloudevent;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.cloudevents.CloudEvent;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public final class SalesforceContextParser {
    private static final Gson gson = new Gson();

    public static Optional<SalesforceContext> parseSalesforceContext(CloudEvent cloudEvent) {
        return parseBase64JsonExtension(cloudEvent, "sfcontext", SalesforceContext.class);
    }

    public static Optional<SalesforceFunctionContext> parseSalesforceFunctionContext(CloudEvent cloudEvent) {
        return parseBase64JsonExtension(cloudEvent, "sffncontext", SalesforceFunctionContext.class);
    }

    private static <A> Optional<A> parseBase64JsonExtension(CloudEvent cloudEvent, String extensionName, Class<A> clazz) {
        Object sfContextExtensionObject = cloudEvent.getExtension(extensionName);
        if (!(sfContextExtensionObject instanceof String)) {
            return Optional.empty();
        }

        byte[] base64DecodedExtension = Base64.getDecoder().decode((String) sfContextExtensionObject);
        String extensionString = new String(base64DecodedExtension, StandardCharsets.UTF_8);

        try {
            return Optional.of(gson.fromJson(extensionString, clazz));
        } catch (JsonSyntaxException e) {
            return Optional.empty();
        }
    }
}
