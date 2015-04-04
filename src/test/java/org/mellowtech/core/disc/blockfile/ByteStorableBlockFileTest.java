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

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.disc.blockfile.ByteStorableBlockFile;
import org.mellowtech.core.util.Platform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Date: 2013-01-23
 * Time: 22:17
 *
 * @author Martin Svensson
 */
public class ByteStorableBlockFileTest {

  //@Test
  public void test() throws IOException {
    String path = Platform.getTempDir();
    String fileName = path+"/BlockFileWithId.blf";
    int numBlocksToCache = 2;
    ByteStorable template = new CBString("");
    int blockSize = 1024;

    ByteStorableBlockFile bsf = new ByteStorableBlockFile(fileName, blockSize,
            template, numBlocksToCache);

    // read a set of strings and simply add them to the file
    TreeMap<String, Integer> map = new TreeMap<String, Integer>();
    URL url = this.getClass().getResource("/com/mellowtech/core/sort/longText.txt");
    File f = new File(url.getFile());
    Scanner sc = new Scanner(f);
    Pattern p = Pattern.compile("\\W+");
    sc.useDelimiter(p);
    int count = 0;

    //read strings:
    while (sc.hasNext()) {
      String s = sc.next().toLowerCase().trim();
      if (!map.containsKey(s))
        map.put(s, new Integer(++count));
    }
    sc.close();

    Map <Integer, Integer> blockMap = new HashMap();
    for (Iterator<Map.Entry<String, Integer>> iter = map.entrySet().iterator(); iter
            .hasNext();) {
      Map.Entry<String, Integer> e = iter.next();
      int blockno = bsf.write(e.getValue(), new CBString(e.getKey()));
      Assert.assertTrue(blockno > -1);
      blockMap.put(e.getValue(), blockno);
    }
    bsf.close();


    //reopen the file:
    bsf = new ByteStorableBlockFile(fileName, blockSize, template,
            numBlocksToCache);

    //now iterate
    for(Map.Entry<String,Integer> entry : map.entrySet()){
      int blockNo = blockMap.get(entry.getValue());
      CBString str = (CBString) bsf.read(blockNo, entry.getValue());
      Assert.assertEquals(entry.getKey(), equals(str.get()));
    }
    bsf.close();
    bsf.deleteFile();
  }
}
