/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sfjavafunction;

import com.google.common.net.MediaType;

/**
 * Result of a {@link SalesforceFunction} invocation. Even though customers define functions with nice, concrete types,
 * the result is a byte array with a media type. {@link SalesforceFunction} takes care of the marshalling process.
 */
public class SalesforceFunctionResult {
    private final MediaType mediaType;
    private final byte[] data;

    public SalesforceFunctionResult(MediaType mediaType, byte[] data) {
        this.mediaType = mediaType;
        this.data = data;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public byte[] getData() {
        return data;
    }
}
