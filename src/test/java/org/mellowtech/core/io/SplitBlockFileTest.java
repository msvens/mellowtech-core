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

package org.mellowtech.core.io;

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.io.BlockFile;
import org.mellowtech.core.io.Record;
import org.mellowtech.core.io.SplitBlockFile;
import org.mellowtech.core.util.Platform;

import java.io.File;
import java.util.Iterator;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
public class SplitBlockFileTest {

  @Test
  public void test() throws Exception{
    String tmpFile = Platform.getTempDir()+"/splitBlockFileTest"+BlockFile.FILE_EXT;
    File f = new File(tmpFile);
    if(f.exists()) f.delete();

    SplitBlockFile rf = new SplitBlockFile(tmpFile, 1024, 1024, 40, 100, 512);
    byte[] test = "This is My Test Record".getBytes();
    CBString reserved = new CBString("this is my reserved");
    rf.setReserve(reserved.toBytes().array());
    rf.insert(test);
    rf.insert(test);
    rf.insert(test);
    rf.insertRegion(test);
    rf.insertRegion(test);

    rf.delete(1);
    Iterator<Record> iter = rf.iterator();
    while(iter.hasNext()){
      Record next = iter.next();
      String str = new String(next.data, 0, test.length);
      Assert.assertEquals("This is My Test Record", str);
    }
    iter = rf.iteratorRegion();

    while(iter.hasNext()){
      Record next = iter.next();
      String str = new String(next.data, 0, test.length);
      Assert.assertEquals("This is My Test Record", str);
    }


    Assert.assertTrue(rf.contains(0));
    Assert.assertTrue(rf.containsRegion(0));
    Assert.assertEquals(2, rf.size());
    Assert.assertEquals(2, rf.sizeRegion());

    rf.close();
    rf = new SplitBlockFile(tmpFile);

    iter = rf.iterator();

    while(iter.hasNext()){
      Record next = iter.next();
      String str = new String(next.data, 0, test.length);
      Assert.assertEquals("This is My Test Record", str);
    }
    iter = rf.iteratorRegion();

    while(iter.hasNext()){
      Record next = iter.next();
      String str = new String(next.data, 0, test.length);
      Assert.assertEquals("This is My Test Record", str);
    }

    reserved.set("");
    reserved.fromBytes(rf.getReserve(), 0, false);
    Assert.assertEquals("this is my reserved", reserved.get());
    Assert.assertEquals(2, rf.size());
    Assert.assertEquals(2, rf.sizeRegion());
    Assert.assertFalse(rf.contains(1));
    Assert.assertTrue(rf.contains(2));
    Assert.assertTrue(rf.contains(0));
    Assert.assertTrue(rf.containsRegion(0));
  }
}
