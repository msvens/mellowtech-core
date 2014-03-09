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
package com.mellowtech.core.util;

import java.io.IOException;
import java.util.logging.Level;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.CBInt;
import com.mellowtech.core.bytestorable.CBString;
import com.mellowtech.core.collections.tree.BPlusTree;


/**
 * @author Martin Svensson
 *
 */
public class String2Int2String {
  private BPlusTree keyStringManager;
  private BPlusTree keyIntegerManager;

  public String2Int2String(String filename) throws Exception {
    try {
      keyIntegerManager = new BPlusTree(filename + "integerAskey");
      keyStringManager = new BPlusTree(filename + "stringAskey");
    }
    catch (IOException e) {
      CoreLog.L().log(Level.FINE, "could not open creating btree");
      keyIntegerManager = new BPlusTree(filename + "integerAskey", new CBInt(),
          new CBString(), 2048, 1024);
      keyStringManager = new BPlusTree(filename + "stringAskey",
          new CBString(), new CBInt(), 2048, 1024);
    }
  }

  public boolean contains(String key1) throws Exception {
    return keyStringManager.containsKey(new CBString(key1));
  }

  public boolean contains(int key2) throws Exception {
    return keyIntegerManager.containsKey(new CBInt(key2));
  }

  public void put(String key1, int key2) throws Exception {
    keyStringManager.insert(new CBString(key1), new CBInt(key2), false);
    try {
      keyIntegerManager.insert(new CBInt(key2), new CBString(key1), false);
    }
    catch (Exception e) {
      keyStringManager.delete(new CBString(key1));
      throw e;
    }
  }

  public int getNumberOfKeys() throws Exception {
    return keyStringManager.getNumberOfElements();
  }

  public void update(String key1, int key2) throws Exception {
    keyStringManager.insert(new CBString(key1), new CBInt(key2), true);
    keyIntegerManager.insert(new CBInt(key2), new CBString(key1), true);
  }

  public String intToString(int key) throws Exception {
    CBString str = (CBString) keyIntegerManager.search(new CBInt(key));
    if (str == null)
      return null;
    else
      return str.get();
  }

  public int stringToInt(String key) throws Exception {
    CBInt integer = (CBInt) keyStringManager.search(new CBString(key));
    if (integer == null)
      return Integer.MIN_VALUE;
    return integer.get();
  }

  public void save() throws Exception {
    keyStringManager.save();
    keyIntegerManager.save();
  }

  /**
   * @return
   */
  public int getHighestInteger() {
    // TODO Auto-generated method stub
    return -1;
  }
}
