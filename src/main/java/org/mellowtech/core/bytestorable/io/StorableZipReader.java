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
package org.mellowtech.core.bytestorable.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ByteStorable;

/**
 * Reads ByteStorable objects from a ZIP file.
 * 
 * @author rickard.coster@asimus.se
 * @version 1.0
 */
public class StorableZipReader <E extends ByteStorable <?>> {

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
      bs = (E) bs.fromBytes(ba, 0, true);
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

