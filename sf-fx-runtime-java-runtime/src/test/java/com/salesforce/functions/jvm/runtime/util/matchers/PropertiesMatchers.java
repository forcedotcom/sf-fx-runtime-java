/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.util.matchers;

import java.util.Properties;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class PropertiesMatchers {
  public static Matcher<Properties> hasPropertyAtKey(String key, Matcher<String> valueMatcher) {
    return new TypeSafeDiagnosingMatcher<Properties>() {
      @Override
      protected boolean matchesSafely(Properties properties, Description description) {
        String value = properties.getProperty(key);
        if (value != null) {
          if (valueMatcher.matches(value)) {
            return true;
          } else {
            description.appendText(
                String.format("was a Properties object with key '%s' whose value ", key));
            valueMatcher.describeMismatch(value, description);
            return false;
          }
        } else {
          description.appendText(String.format("was a Properties object without key '%s'", key));
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText(String.format("a Properties object with key '%s' whose value ", key))
            .appendDescriptionOf(valueMatcher);
      }
    };
  }

  private PropertiesMatchers() {}
}
