/*
 * Copyright 2015 mellowtech.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mellowtech.core.bytestorable.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.mellowtech.core.bytestorable.BStorable;

/**
 * Reads BStorable objects from a ZIP file. Each BStorable object
 * is stored in its own zip entry.
 *
 * @param <A> Wrapped BStorable class
 *
 * @author Rickard Cöster {@literal rickcos@gmail.com}
 * @since 3.0.1
 */
public class StorableZipReader <A> {

  ZipFile zipFile;
  BStorable<A> template;
  Enumeration <? extends ZipEntry> en;

  /**
   * Create a new reader using the given template
   * @param fileName path to zip file
   * @param template BStorable template
   * @throws IOException if an exception occurs
   */
  public StorableZipReader(String fileName, BStorable<A> template)
      throws IOException {
    zipFile = new ZipFile(new File(fileName));
    this.template = template;
    reset();
  }

  /**
   * Read the BStorable object at entry
   * @param name entry name
   * @return BStorable object
   * @throws IOException if an exception occurs
   */
  public BStorable<A> get(String name) throws IOException {
    ZipEntry ze = zipFile.getEntry(name);
    if (ze == null)
      return null;
    return get(ze);
  }

  protected BStorable<A> get(ZipEntry ze) throws IOException {
    InputStream is = zipFile.getInputStream(ze);
    byte[] ba = new byte[(int) ze.getSize()];
    int c, offset = 0;
    while ((c = is.read()) != -1)
      ba[offset++] = (byte) c;

    BStorable<A> bs = null;
    try {
      bs = template.from(ba, 0);
    }
    catch (Exception e) {
      throw new IOException(e);
    }
    return bs;
  }

  /**
   * Read the next BStorable from this zip files entries
   * @return BStorable object or null if no more entries are found
   * @throws IOException if an exception occurs
   */
  public BStorable<A> next() throws IOException {
    if (!en.hasMoreElements())
      return null;
    ZipEntry ze = (ZipEntry) en.nextElement();
    return get(ze);
  }

  /**
   * Reset this reader to point to the first entry in the file
   * @throws IOException if an exception occurs
   */
  public void reset() throws IOException {
    en = zipFile.entries();
  }

  /**
   * Close this reader
   * @throws IOException if an exception occurs while closing the underlying zip file
   */
  public void close() throws IOException {
    zipFile.close();
  }
}

