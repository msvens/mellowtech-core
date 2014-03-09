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

package com.mellowtech.core.disc.blockfile;

import java.io.IOException;

import com.mellowtech.core.bytestorable.ext.AutoByteStorable;
import com.mellowtech.core.bytestorable.CBInt;
import com.mellowtech.core.bytestorable.ext.IntegerSet;
import com.mellowtech.core.disc.StorableFile;

public class DynamicFileInfo extends AutoByteStorable {
  public CBInt smallObjectMaxBytes = new CBInt();
  public CBInt smallObjectBlockSize = new CBInt();
  public CBInt largeObjectBlockSize = new CBInt();
  public CBInt highestId = new CBInt();
  public IntegerSet freeSmallObjectIds = new IntegerSet(1024);

  public static DynamicFileInfo read(String file) throws IOException {
    return (DynamicFileInfo) StorableFile.readFileAsByteStorable(file,
        new DynamicFileInfo());
  }

  public static void write(String file, DynamicFileInfo fileInfo)
      throws IOException {
    StorableFile.writeFileAsByteStorable(file, fileInfo);
  }
}
