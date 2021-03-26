/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.json;

import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import org.junit.Test;
import static org.junit.Assert.*;

public class GsonJsonLibraryTest {

    @Test
    public void testDeserialization() throws Exception {
        JsonLibrary jsonLibrary = new GsonJsonLibrary();
        Object testClass = jsonLibrary.deserializeAt("{\"foo\": \"bar\"}", TestClass.class);
        assertEquals(((TestClass) testClass).getFoo(), "bar");
    }

    @Test(expected = JsonDeserializationException.class)
    public void testExceptionWrapping() throws Exception {
        JsonLibrary jsonLibrary = new GsonJsonLibrary();
        jsonLibrary.deserializeAt("{\"foo: \"bar\"}", Test.class);
    }

    public static class TestClass {
        private String foo;

        public String getFoo() {
            return foo;
        }
    }
}