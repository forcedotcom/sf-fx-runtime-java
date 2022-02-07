/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class UtilTest {

  @Test
  public void test() throws Exception {
    List<Annotation> annotationList =
        Util.getAnnotationsOnClassFieldsAndMethods(TestClassOne.class);

    assertThat(
        annotationList,
        containsInAnyOrder(
            equalTo(createAnnotation(Foo.class, "publicField")),
            equalTo(createAnnotation(Bar.class, "privateField")),
            equalTo(createAnnotation(Foo.class, "publicMethod")),
            equalTo(createAnnotation(Bar.class, "privateMethod"))));
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
    @Foo(data = "publicField")
    public String publicField;

    @Bar(data = "privateField")
    private String privateField;

    @Foo(data = "publicMethod")
    public void publicMethod() {}

    @Bar(data = "privateMethod")
    public void privateMethod() {}
  }

  private <A extends Annotation> A createAnnotation(Class<A> clazz, String data)
      throws AnnotationFormatException {
    Map<String, Object> map = new HashMap<>();
    map.put("data", data);

    return TypeFactory.annotation(clazz, map);
  }
}
