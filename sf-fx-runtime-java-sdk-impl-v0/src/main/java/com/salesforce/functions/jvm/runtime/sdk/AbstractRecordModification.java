/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sdk;

import com.google.gson.JsonPrimitive;
import com.salesforce.functions.jvm.sdk.data.RecordModification;
import com.salesforce.functions.jvm.sdk.data.ReferenceId;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRecordModification<T extends RecordModification<T>> implements RecordModification<T> {
    // TODO: This MUST be JsonElement since JsonPrimitives cannot be null!
    protected final Map<String, JsonPrimitive> values;

    public AbstractRecordModification(Map<String, JsonPrimitive> values) {
        this.values = values;
    }

    protected abstract T copy(Map<String, JsonPrimitive> values);

    @Override
    public T setStringValue(String key, String value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setShortValue(String key, short value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setNumberValue(String key, Number value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setLongValue(String key, long value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setIntValue(String key, int value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setFloatValue(String key, float value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setDoubleValue(String key, double value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setCharacterValue(String key, char value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setByteValue(String key, byte value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setBooleanValue(String key, boolean value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setBigIntegerValue(String key, BigInteger value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setBigDecimalValue(String key, BigDecimal value) {
        return setValue(key, new JsonPrimitive(value));
    }

    @Override
    public T setReferenceIdValue(String key, ReferenceId fkId) {
        return setStringValue(key, fkId.toApiString());
    }

    protected T setValue(String key, JsonPrimitive value) {
        final HashMap<String, JsonPrimitive> copiedValues = new HashMap<>(values);
        copiedValues.put(key, value);
        return copy(copiedValues);
    }

    protected Map<String, JsonPrimitive> getValues() {
        return Collections.unmodifiableMap(values);
    }
}
