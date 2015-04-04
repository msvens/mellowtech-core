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

import org.mellowtech.core.util.Platform;

public class BlockUtilization {
  int numBlocks;
  int blockSize;
  int dataLen;
  int idLen;
  int count;

  void add(BlockUtilization u) {
    numBlocks += u.numBlocks;
    blockSize = u.blockSize; // same block size
    dataLen += u.dataLen;
    idLen += u.idLen;
    count += u.count;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    String sep = Platform.getLineSeparator();
    sb.append("BlockSize = " + blockSize + sep);
    sb.append("NumBlocks = " + numBlocks + sep);
    sb.append("DataLen   = " + dataLen + sep);
    sb.append("IdLen     = " + idLen + sep);
    sb.append("Count     = " + count + sep);
    double u = dataLen / ((double) (blockSize * numBlocks));
    sb.append("DataLen/(BlockSize*NumBlocks) " + u + sep);
    u = (dataLen + idLen) / ((double) (blockSize * numBlocks));
    sb.append("(DataLen+idLen)/(BlockSize*NumBlocks) " + u + sep);
    return sb.toString();
  }

}
