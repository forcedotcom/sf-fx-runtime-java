/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.maven;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.salesforce.functions.jvm.runtime.test.Util;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class HelpEvaluateMavenInvocationOutputHandlerTest {
  @Test
  public void test() throws IOException {
    MavenInvocationOutputHandler<Optional<String>> outputHandler =
        new HelpEvaluateMavenInvocationOutputHandler();

    List<String> lines = Util.readLinesFromResource("maven-help-evaluate-output.txt");

    for (String line : lines) {
      outputHandler.consumeLine(line);
    }

    assertThat(
        outputHandler.getResult(),
        is(
            optionalWithValue(
                equalTo("/Users/manuel.fuchs/projects/sf-fx-runtime-java/target/classes"))));
  }
}
