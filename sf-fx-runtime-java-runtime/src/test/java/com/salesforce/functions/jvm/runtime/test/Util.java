/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class Util {
  public static Path downloadToTemporary(String url) throws IOException {
    Path temporaryFilePath = Files.createTempFile("test_", ".jar");
    ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
    FileOutputStream fileOutputStream = new FileOutputStream(temporaryFilePath.toFile());
    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

    return temporaryFilePath;
  }
}
