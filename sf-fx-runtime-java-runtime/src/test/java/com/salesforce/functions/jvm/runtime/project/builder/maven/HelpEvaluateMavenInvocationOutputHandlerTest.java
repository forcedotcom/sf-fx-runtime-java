/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.maven;

import com.salesforce.functions.jvm.runtime.test.Util;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
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

    Assert.assertEquals(
        Optional.of("/Users/manuel.fuchs/projects/sf-fx-runtime-java/target/classes"),
        outputHandler.getResult());
  }
}
