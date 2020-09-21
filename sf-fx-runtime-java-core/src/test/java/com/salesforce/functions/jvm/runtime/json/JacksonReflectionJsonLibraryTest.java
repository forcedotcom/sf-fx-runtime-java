package com.salesforce.functions.jvm.runtime.json;

import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JacksonReflectionJsonLibraryTest {

    @Test
    public void testDeserialization() throws Exception {
        JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
        Object testClass = jsonLibrary.deserializeAt("{\"foo\": \"bar\"}", TestClass.class);
        assertEquals(((TestClass) testClass).getFoo(), "bar");
    }

    @Test(expected = JsonDeserializationException.class)
    public void testExceptionWrapping() throws Exception {
        JsonLibrary jsonLibrary = new JacksonReflectionJsonLibrary(getClass().getClassLoader());
        jsonLibrary.deserializeAt("{\"foo: \"bar\"}", Test.class);
    }

    public static class TestClass {
        public String foo;

        public String getFoo() {
            return foo;
        }
    }
}
