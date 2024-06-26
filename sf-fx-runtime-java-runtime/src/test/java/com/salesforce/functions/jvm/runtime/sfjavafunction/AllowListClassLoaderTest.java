/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.salesforce.functions.jvm.runtime.project.util.ProjectClassLoaderBuilder;
import com.salesforce.functions.jvm.runtime.test.Util;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class AllowListClassLoaderTest {
  private final ClassLoader exposedClassLoader;
  private final ClassLoader parentClassLoader;
  private final ClassLoader allowListClassLoader;

  private final List<String> classesOnlyInParentClassLoader =
      Collections.singletonList("org.apache.log4j.ConsoleAppender");

  private final List<String> classesOnlyInExposedClassLoader =
      Arrays.asList(
          "com.salesforce.functions.jvm.runtime.SalesforceFunctionsJvmRuntime",
          "com.salesforce.functions.jvm.runtime.test.Util");

  private final List<String> allowList =
      Collections.singletonList("com.salesforce.functions.jvm.runtime.sfjavafunction");

  private final List<String> classesAffectedByAllowList =
      Collections.singletonList(
          "com.salesforce.functions.jvm.runtime.sfjavafunction.AllowListClassLoaderTest");

  public AllowListClassLoaderTest() throws Exception {
    exposedClassLoader = getClass().getClassLoader();

    Path log4jJarPath =
        Util.downloadFileToTemporary(
            "https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar");
    parentClassLoader = ProjectClassLoaderBuilder.build(Collections.singletonList(log4jJarPath));

    allowListClassLoader =
        new AllowListClassLoader(
            exposedClassLoader, parentClassLoader, allowList.toArray(new String[0]));
  }

  @Test
  public void testParentClassLoading() throws Exception {
    for (String className : classesOnlyInParentClassLoader) {
      parentClassLoader.loadClass(className);
    }
  }

  @Test
  public void testExposedClassLoading() throws Exception {
    for (String className : classesOnlyInExposedClassLoader) {
      exposedClassLoader.loadClass(className);
    }
  }

  @Test
  public void testExposedClassesNotAvailableInParent() {
    for (String className : classesOnlyInExposedClassLoader) {
      try {
        parentClassLoader.loadClass(className);
        Assert.fail(className + " should not load with parent class loader!");
      } catch (ClassNotFoundException e) {
        // Ignore, we expect this exception to be thrown and fail with Assert.fail when the class
        // could be loaded.
      }
    }
  }

  @Test
  public void testParentClassesNotAvailableInExposed() {
    for (String className : classesOnlyInParentClassLoader) {
      try {
        exposedClassLoader.loadClass(className);
        Assert.fail(className + " should not load with exposed class loader!");
      } catch (ClassNotFoundException e) {
        // Ignore, we expect this exception to be thrown and fail with Assert.fail when the class
        // could be loaded.
      }
    }
  }

  @Test
  public void testLoadingParentClassesViaAllowListClassLoader() throws Exception {
    for (String className : classesOnlyInParentClassLoader) {
      allowListClassLoader.loadClass(className);
    }
  }

  @Test
  public void testLoadingAllowListedClassesViaAllowListClassLoader() throws Exception {
    for (String className : classesAffectedByAllowList) {
      Class<?> allowListClass = allowListClassLoader.loadClass(className);
      Class<?> exposedClass = exposedClassLoader.loadClass(className);

      assertThat(exposedClass, is(allowListClass));
    }
  }
}
