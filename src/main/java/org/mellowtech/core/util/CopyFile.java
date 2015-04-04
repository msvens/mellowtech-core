/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package org.mellowtech.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Utility to copy one file to another or append a file onto another file.
 */

public class CopyFile {
  /** Copies a file from pIn to pOut using optimal nio. */
  public static void copy(File pIn, File pOut) throws IOException {
    FileChannel sourceChannel = new FileInputStream(pIn).getChannel();
    FileChannel destChannel = new FileOutputStream(pOut).getChannel();

    sourceChannel.transferTo(0, sourceChannel.size(), destChannel);

    sourceChannel.close();
    destChannel.close();
  } // copy

  /** Appends pUpdate onto end of pMain using optimal nio. */
  public static void append(File pMain, File pUpdate) throws IOException {
    FileChannel mainChannel = new FileOutputStream(pMain, true).getChannel();
    FileChannel updateChannel = new FileInputStream(pUpdate).getChannel();

    // Position last in main so append goes there.
    mainChannel.position(pMain.length());

    // Transfer update to end of main.
    mainChannel.transferFrom(updateChannel, 0, updateChannel.size());

    mainChannel.force(true);
    mainChannel.close();
    updateChannel.close();
  } // append

  /**
   * Moves a file from one place to the other. It 1st tries just to rename the
   * file. If this fails then a fast nio copy is done.
   */
  public static void move(File pFrom, File pTo) throws IOException {
    if (pFrom.renameTo(pTo) == true)
      return;
    CopyFile.copy(pFrom, pTo);
  }
} // CopyFile
