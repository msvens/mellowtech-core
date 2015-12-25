/*
 * Copyright 2015 mellowtech.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mellowtech.core.collections.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.bytestorable.CBBitSet;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBList;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.impl.ObjectManager;
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
      m.put("5", new CBString("hello world"));
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
