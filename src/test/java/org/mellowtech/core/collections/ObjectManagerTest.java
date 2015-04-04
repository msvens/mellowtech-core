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

package org.mellowtech.core.collections;

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.bytestorable.CBBitSet;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBList;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.ObjectManager;
import org.mellowtech.core.util.DelDir;
import org.mellowtech.core.util.Platform;

import java.io.File;

/**
 * Date: 2013-01-15
 * Time: 22:10
 *
 * @author Martin Svensson
 */
public class ObjectManagerTest {

  @Test
  public void test() throws Exception{
    File f = new File(Platform.getTempDir()+"/objmantest1");
    try {
      //DelDir.d(f.getAbsolutePath());
      Assert.assertFalse(f.exists());
      f.mkdir();

      ObjectManager m = new ObjectManager(f.getAbsolutePath()+"/manager", true, false);
      m.put("5", new CBString(new java.util.Date().toString()));
      m.put("6", new CBInt(100));
      m.put("7", new CBList <String>());
      m.put("8", new CBBitSet());


      Assert.assertEquals(m.get("5").getClass().getName(),
              new CBString().getClass().getName());
      
      Assert.assertEquals(m.get("6").getClass().getName(),
              new CBInt().getClass().getName());
      Assert.assertEquals(m.get("7").getClass().getName(),
              new CBList <String> ().getClass().getName());
      Assert.assertEquals(m.get("8").getClass().getName(),
              new CBBitSet().getClass().getName());
      m.save();
      m = null;
    } catch (Exception e) {
      DelDir.d(f.getAbsolutePath());
      throw new Exception(e);
    }
    DelDir.d(f.getAbsolutePath());
  }
}
