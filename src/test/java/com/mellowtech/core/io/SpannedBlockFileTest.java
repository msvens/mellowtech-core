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

package com.mellowtech.core.io;

import com.mellowtech.core.bytestorable.CBString;
import com.mellowtech.core.util.Platform;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Date: 2013-03-11
 * Time: 14:43
 *
 * @author Martin Svensson
 */
public class SpannedBlockFileTest {

  @Test
  public void test() throws Exception{
    String tmpFile = Platform.getTempDir()+"/spannedBlockFileTest"+BlockFile.FILE_EXT;
    File f = new File(tmpFile);
    if(f.exists()) f.delete();

    SpannedBlockFile rf = new SpannedBlockFile(tmpFile, 512, 1024, 40);
    byte[] test = new CBString("This is My Test Record").toBytes().array();
    CBString reserved = new CBString("this is my reserved");
    rf.setReserve(reserved.toBytes().array());
    int b1 = rf.insert(test);
    int b2 = rf.insert(test);
    byte[] test1 = new byte[1034];
    test1[test1.length-1] = 1;
    int b3 = rf.insert(test1);
    int b4 = rf.insert(test1);
    rf.delete(b2);
    
    
    byte[] b = rf.get(b1);
    //System.out.println(b.length);
    Assert.assertEquals(new CBString("This is My Test Record"), new CBString().fromBytes(b, 0));
    b = rf.get(b3);
    Assert.assertEquals(1034, b.length);
    Assert.assertEquals(1, b[b.length-1]);
    b = rf.get(b4);
    Assert.assertEquals(1034, b.length);
    Assert.assertEquals(1, b[b.length-1]);
    
    rf.close();
    rf = new SpannedBlockFile(tmpFile, 1024, 1024, 0);
    
    b = rf.getReserve();
    Assert.assertEquals(new CBString("this is my reserved"), new CBString().fromBytes(b, 0));
    b = rf.get(b1);
    Assert.assertEquals(new CBString("This is My Test Record"), new CBString().fromBytes(b, 0));
    b = rf.get(b3);
    Assert.assertEquals(1034, b.length);
    Assert.assertEquals(1, b[b.length-1]);
    b = rf.get(b4);
    Assert.assertEquals(1034, b.length);
    Assert.assertEquals(1, b[b.length-1]);
    
  }
}
