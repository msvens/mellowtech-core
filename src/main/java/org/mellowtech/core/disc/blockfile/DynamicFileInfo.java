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

package org.mellowtech.core.disc.blockfile;

import java.io.IOException;
import java.util.BitSet;

import org.mellowtech.core.bytestorable.AutoRecord;
import org.mellowtech.core.bytestorable.BSField;
import org.mellowtech.core.bytestorable.CBRecord;
import org.mellowtech.core.bytestorable.io.StorableFile;

public class DynamicFileInfo extends CBRecord <DynamicFileInfo.Record> {
  
  public class Record implements AutoRecord {
  
    @BSField(1) public int smallObjectMaxBytes;
    @BSField(2) public int smallObjectBlockSize;
    @BSField(3) public int largeObjectBlockSize;
    @BSField(4) public int highestId;
    @BSField(5) public BitSet freeSmallObjectIds = new BitSet(1024);
  }
  
  
 /* public CBInt smallObjectMaxBytes = new CBInt();
  public CBInt smallObjectBlockSize = new CBInt();
  public CBInt largeObjectBlockSize = new CBInt();
  public CBInt highestId = new CBInt();
  public IntegerSet freeSmallObjectIds = new IntegerSet(1024);
  */
  
  public static DynamicFileInfo read(String file) throws IOException {
    return (DynamicFileInfo) StorableFile.readFileAsByteStorable(file,
        new DynamicFileInfo());
  }

  public static void write(String file, DynamicFileInfo fileInfo)
      throws IOException {
    StorableFile.writeFileAsByteStorable(file, fileInfo);
  }
  
  public DynamicFileInfo() {
    super();
  }

  @Override
  protected Record newT() {
    return new Record();
  }
}
