/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.json;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import org.junit.Test;

public class JsonLibraryDetectorTest {

  @Test
  public void testGenericJsonLibrary() throws Exception {
    JsonLibrary lib = JsonLibraryDetector.detect(GenericJsonClass.class);
    if (!(lib instanceof GsonJsonLibrary)) {
      fail("Expected GsonJsonLibrary for generic classes");
    }
  }

  @Test
  public void testGsonJsonLibrary() throws Exception {
    JsonLibrary lib = JsonLibraryDetector.detect(GsonJsonClass.class);
    if (!(lib instanceof GsonReflectionJsonLibrary)) {
      fail("Expected GsonReflectionJsonLibrary for classes with Gson annotations");
    }
  }

  @Test
  public void testJacksonJsonLibrary() throws Exception {
    JsonLibrary lib = JsonLibraryDetector.detect(JacksonJsonClass.class);
    if (!(lib instanceof JacksonReflectionJsonLibrary)) {
      fail("Expected JacksonReflectionJsonLibrary for classes with Jackson annotations");
    }
  }

  @Test(expected = AmbiguousJsonLibraryException.class)
  public void testAmbiguousJsonLibrary() throws Exception {
    JsonLibraryDetector.detect(AmbiguousJsonClass.class);
  }

  @Test
  public void testBootstrapClass() throws Exception {
    JsonLibrary lib = JsonLibraryDetector.detect(String.class);
    if (!(lib instanceof GsonJsonLibrary)) {
      fail("Expected GsonJsonLibrary for bootstrap classes");
    }
  }

  private static class GenericJsonClass {
    private String value1;
    private int value2;
  }

  private static class JacksonJsonClass {
    private String value1;

    @JsonProperty("value")
    private int value2;
  }

  private static class GsonJsonClass {
    private String value1;

    @SerializedName("value")
    private int value2;
  }

  private static class AmbiguousJsonClass {
    @JsonProperty("valueOne")
    private String value1;

    @SerializedName("valueTwo")
    private int value2;
  }
}
