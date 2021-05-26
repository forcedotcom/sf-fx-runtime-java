/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.util;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;

public class ClassLoaderUtilsTest {

  @Test
  public void testCopyFileFromClassLoader() throws IOException {
    Path temporaryDirectory = Files.createTempDirectory("classloadutilstest");
    Path temporaryFile = Files.createTempFile(temporaryDirectory, "", ".tmp");

    String randomString = UUID.randomUUID().toString();
    Files.write(temporaryFile, randomString.getBytes(StandardCharsets.UTF_8));

    URL[] urls = new URL[] {temporaryDirectory.toUri().toURL()};
    ClassLoader classLoader = new URLClassLoader(urls);

    Optional<Path> result =
        ClassLoaderUtils.copyFileFromClassLoader(
            classLoader, temporaryFile.getFileName().toString());

    assertThat(result, is(optionalWithValue(not(equalTo(temporaryFile)))));

    assertThat(
        Files.readAllBytes(result.get()),
        is(equalTo(randomString.getBytes(StandardCharsets.UTF_8))));
  }
}
