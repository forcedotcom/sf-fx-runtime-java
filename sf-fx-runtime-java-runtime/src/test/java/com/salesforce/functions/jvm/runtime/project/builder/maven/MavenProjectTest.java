/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.project.builder.maven;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class MavenProjectTest {

  @Test
  public void testTypeName() {
    MavenProject project = new MavenProject(Collections.emptyList());
    assertThat(project.getTypeName(), is(equalTo("Maven")));
  }

  @Test
  public void testGetPaths() {
    List<Path> paths = new ArrayList<>();
    paths.add(Paths.get("tmp", "foo", "bar"));
    paths.add(Paths.get("tmp", "zzz", "baz"));

    MavenProject project = new MavenProject(paths);
    assertThat(
        project.getClasspathPaths(),
        hasItems(Paths.get("tmp", "foo", "bar"), Paths.get("tmp", "zzz", "baz")));
  }
}
