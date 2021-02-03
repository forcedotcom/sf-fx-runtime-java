/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.json.JsonLibrary;
import com.salesforce.functions.jvm.runtime.json.JsonLibraryDetector;
import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.PayloadUnmarshallingException;
import io.cloudevents.CloudEvent;

import java.nio.charset.StandardCharsets;

public class PojoFromJsonPayloadUnmarshaller implements PayloadUnmarshaller {
    private final Class<?> clazz;
    private final JsonLibrary jsonLibrary;

    public PojoFromJsonPayloadUnmarshaller(Class<?> clazz) throws AmbiguousJsonLibraryException {
        this.clazz = clazz;
        this.jsonLibrary = JsonLibraryDetector.detect(clazz);
    }

    @Override
    public MediaType getHandledMediaType() {
        return MediaType.JSON_UTF_8;
    }

    @Override
    public Class<?> getTargetClass() {
        return clazz;
    }

    @Override
    public Object unmarshall(CloudEvent cloudEvent) throws PayloadUnmarshallingException {
        if (cloudEvent.getData() == null) {
            throw new PayloadUnmarshallingException("Cannot unmarshall without any data!");
        }

        // RFC 4627 and RFC 7159 both state that the default encoding for JSON is UTF-8. RFC 7159 even goes so far
        // to encourage implementations to only use UTF-8 for maximum interoperability. We only support UTF-8 here
        // for the same reasons since, especially when dealing with RFC 7159 JSON, detecting the charset is very
        // hard. Please note that the JSON mime-type does not allow for a charset attribute that we can use.
        //
        // Invalid UTF-8 is not a concern here, when Java's parser detects invalid data, it will insert Unicode's
        // replacement character and continue to parse the data. There won't be an exception but actual JSON parsing
        // will most likely fail later on.
        String cloudEventDataUtf8String = new String(cloudEvent.getData().toBytes(), StandardCharsets.UTF_8);

        try {
            return jsonLibrary.deserializeAt(cloudEventDataUtf8String, clazz);
        } catch (JsonDeserializationException e) {
            throw new PayloadUnmarshallingException("Could not unmarshall payload!", e);
        }
    }
}
