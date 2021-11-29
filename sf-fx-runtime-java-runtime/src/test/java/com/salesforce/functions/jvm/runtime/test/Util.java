/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Util {

  public static void downloadFile(String url, Path path) throws IOException {
    ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
    FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    fileOutputStream.close();
  }

  public static Path downloadFileToTemporary(String url) throws IOException {
    Path temporaryFilePath = Files.createTempFile("test_", ".jar");
    downloadFile(url, temporaryFilePath);
    return temporaryFilePath;
  }

  public static List<String> readLinesFromResource(String name) throws IOException {
    InputStream inputStream = Util.class.getClassLoader().getResourceAsStream(name);
    if (inputStream == null) {
      return Collections.emptyList();
    }

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.toList());
    }
  }
}
