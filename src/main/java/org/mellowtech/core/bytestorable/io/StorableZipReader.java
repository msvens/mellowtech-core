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

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BStorable;

/**
 * Reads ByteStorable objects from a ZIP file.
 * 
 * @author rickard.coster@asimus.se
 * @version 1.0
 */
public class StorableZipReader <A,E extends BStorable <A,E>> {

  ZipFile zipFile;
  E template;
  Enumeration <? extends ZipEntry> en;

  public StorableZipReader(String fileName, E template)
      throws IOException {
    zipFile = new ZipFile(new File(fileName));
    this.template = template;
    reset();
  }

  public E get(String name) throws IOException {
    ZipEntry ze = zipFile.getEntry(name);
    if (ze == null)
      return null;
    return get(ze);
  }

  protected E get(ZipEntry ze) throws IOException {
    InputStream is = zipFile.getInputStream(ze);
    byte[] ba = new byte[(int) ze.getSize()];
    int c, offset = 0;
    while ((c = is.read()) != -1)
      ba[offset++] = (byte) c;

    E bs = null;
    try {
      bs = template.from(ba, 0);
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
    return bs;
  }

  public E next() throws IOException {
    if (!en.hasMoreElements())
      return null;
    ZipEntry ze = (ZipEntry) en.nextElement();
    return get(ze);
  }

  public void reset() throws IOException {
    en = zipFile.entries();
  }

  public void close() throws IOException {
    zipFile.close();
  }

} // StorableZipReader

