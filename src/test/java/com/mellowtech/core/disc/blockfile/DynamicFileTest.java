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

import com.mellowtech.core.bytestorable.ext.ByteStorableId;
import com.mellowtech.core.bytestorable.CBByteArray;
import com.mellowtech.core.util.DelDir;
import com.mellowtech.core.util.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Random;

/**
 * Date: 2013-01-26
 * Time: 11:10
 *
 * @author Martin Svensson
 */
public class DynamicFileTest {

  public static final String testDir = Platform.getTempDir() + "/dynFileTest";

  @Before
  public void setup() {
    File f = new File(testDir);
    f.mkdirs();
  }

  @After
  public void tearDown() {
    DelDir.d(testDir);
  }

  @Test
  public void test() throws Exception {
    int sizeMin = 64;
    int[] sizesMax = new int[]{/*128, 256, 512,*/ 1024, 2048, 4096, 8192, 16384};
    int n = 1000;
    Random random = new Random();
    DynamicFilePointer[][] ptrs = new DynamicFilePointer[sizesMax.length][n];
    for (int k = 0; k < sizesMax.length; k++) {
      int sizeMax = sizesMax[k];
      final String filePrefix = "DynFile-" + sizeMax;
      String fileName = testDir + "/" + filePrefix;

      DynamicFile f = new DynamicFile(fileName,
              sizeMax, sizeMax * 2, sizeMax * 4, new ByteStorableId(0, new CBByteArray()));

      long bytesStored = 0L;
      long bytesPtr = 0L;
      for (int i = 1; i <= n; i++) {
        int size = sizeMin + random.nextInt(sizeMax - sizeMin);
        byte[] bb = new byte[size];
        for (int b = 0; b < bb.length; b++)
          bb[b] = (byte) b;
        CBByteArray bs = new CBByteArray(bb);
        ByteStorableId ibs = new ByteStorableId(i, bs);

        ptrs[k][i - 1] = f.insert(ibs);
        bytesStored += ibs.byteSize();
        bytesPtr += ptrs[k][i - 1].byteSize();
      }

      f.close();
    }

    for (int k = 0; k < sizesMax.length; k++) {
      long start = System.currentTimeMillis();
      int sizeMax = sizesMax[k];
      final String filePrefix = "DynFile-" + sizeMax;
      String fileName = testDir +"/"+ filePrefix;
      DynamicFile f = new DynamicFile(fileName, new ByteStorableId(0, new CBByteArray()));
      long bytesStored = 0L;
      long bytesPtr = 0L;

      for (int i = 1; i <= n; i++) {
          ByteStorableId ibs = (ByteStorableId) f.get(ptrs[k][i - 1]);
          bytesStored += ibs.byteSize();
          bytesPtr += ptrs[k][i - 1].byteSize();
      }
    }
  }
}
