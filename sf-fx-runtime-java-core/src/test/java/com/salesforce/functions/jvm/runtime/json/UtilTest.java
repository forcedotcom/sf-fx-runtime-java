package com.salesforce.functions.jvm.runtime.json;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void test() throws Exception {
        List<Annotation> annotationList = Util.getAnnotationsOnClassFieldsAndMethods(TestClassOne.class);

        List<Annotation> expectedAnnotations = new ArrayList<>();
        expectedAnnotations.add(createAnnotation(Foo.class,"publicField"));
        expectedAnnotations.add(createAnnotation(Bar.class,"privateField"));
        expectedAnnotations.add(createAnnotation(Foo.class,"publicMethod"));
        expectedAnnotations.add(createAnnotation(Bar.class,"privateMethod"));

        assertEquals(expectedAnnotations, annotationList);
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Foo {
        String data();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Bar {
        String data();
    }

    private static class TestClassOne {
        @Foo(data="publicField")
        public String publicField;

        @Bar(data="privateField")
        private String privateField;

        @Foo(data="publicMethod")
        public void publicMethod() {
        }

        @Bar(data="privateMethod")
        public void privateMethod() {
        }
    }

    private <A extends Annotation> A createAnnotation(Class<A> clazz, String data) throws AnnotationFormatException {
        Map<String, Object> map = new HashMap<>();
        map.put("data", data);

        return TypeFactory.annotation(clazz, map);
    }
}
