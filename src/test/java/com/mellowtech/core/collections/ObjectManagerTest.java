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

package com.mellowtech.core.collections;

import com.mellowtech.core.bytestorable.ext.CBArrayList;
import com.mellowtech.core.bytestorable.ext.CBBitSet;
import com.mellowtech.core.bytestorable.CBInt;
import com.mellowtech.core.bytestorable.CBString;
import com.mellowtech.core.util.DelDir;
import com.mellowtech.core.util.Platform;
import junit.framework.Assert;
import org.junit.Test;

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
    File f = new File(Platform.getTempDir()+"/objmantest");
    try {
      Assert.assertFalse(f.exists());
      f.mkdir();

      ObjectManager m = new ObjectManager(f.getAbsolutePath());
      m.create(new CBString("5"), new CBString(new java.util.Date().toString()));
      m.create(new CBString("6"), new CBInt(100));
      m.create(new CBString("7"), new CBArrayList(new CBString()));
      m.create(new CBString("8"), new CBBitSet());

      CBString str = new CBString();
      str.set("5");
      Assert.assertEquals(m.get(str).getClass().getName(),
              new CBString().getClass().getName());
      str.set("6");
      Assert.assertEquals(m.get(str).getClass().getName(),
              new CBInt().getClass().getName());
      str.set("7");
      Assert.assertEquals(m.get(str).getClass().getName(),
              new CBArrayList().getClass().getName());
      str.set("8");
      Assert.assertEquals(m.get(str).getClass().getName(),
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
