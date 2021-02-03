/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.FunctionResultMarshallingException;

import java.nio.charset.StandardCharsets;

public class StringFunctionResultMarshaller implements FunctionResultMarshaller {
    @Override
    public MediaType getMediaType() {
        return MediaType.PLAIN_TEXT_UTF_8;
    }

    @Override
    public Class<?> getSourceClass() {
        return String.class;
    }

    @Override
    public byte[] marshallBytes(Object object) throws FunctionResultMarshallingException {
        if (!(object instanceof String)) {
            throw new FunctionResultMarshallingException(
                    String.format("Expected java.lang.String for marshalling, got %s!", object.getClass().getName())
            );
        }

        String string = (String) object;
        return string.getBytes(StandardCharsets.UTF_8);
    }
}
