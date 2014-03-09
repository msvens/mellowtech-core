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
package com.mellowtech.core.collections.tree;

import com.mellowtech.core.TestUtils;
import com.mellowtech.core.bytestorable.CBInt;
import com.mellowtech.core.bytestorable.CBString;
import com.mellowtech.core.collections.DiscBasedMap;
import com.mellowtech.core.collections.mappings.IntegerMapping;
import com.mellowtech.core.collections.mappings.StringMapping;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;


/**
 * @author Martin Svensson
 */
public class BPlusTreeTest {

  public BPlusTree dbMap;
  public TreeMap<String, Integer> inMemoryMap;
  public int numDifferentWords = 1000;
  public int numWords = numDifferentWords * 1;
  String text;
  public static final String dir = "dbmtest";
  public static final String name = "discBasedMap";


  @Before
  public void before() throws Exception {
    TestUtils.createTempDir(dir);


    //Generate test data...
    text = "";
    this.inMemoryMap = new TreeMap<>();

    String fileName = TestUtils.getAbsolutDir(dir+"/"+name);

    this.dbMap = new BPlusTree(fileName, new CBString(), new CBInt(),
            1024, 256);

    this.dbMap.useCache(true, false);

    Random r = new Random();
    for (int i = 0; i < numWords; i++) {

      String word = "" + i;
      text += word + " ";
    }

  }

  @Test
  public void doTest() throws Exception {
    ;
  }

  @After
  public void after() throws Exception {

    this.dbMap.save();
    this.dbMap.deleteTree();
    TestUtils.deleteTempDir(dir);
  }



  private void insert() throws IOException {
    StringTokenizer st = new StringTokenizer(this.text);
    int tot = 0;
    while (st.hasMoreTokens()) {
      tot++;
      String next = st.nextToken();
      Integer count = this.inMemoryMap.get(next);
      if (count == null) {
        this.inMemoryMap.put(next, 1);
        this.dbMap.insert(new CBString(next), new CBInt(1), true);
      } else {
        this.inMemoryMap.put(next, count + 1);
        this.dbMap.insert(new CBString(next), new CBInt(count + 1), true);
      }
    }
  }

  private void testContains() throws IOException{
    for (String str : inMemoryMap.keySet()) {
      boolean contains = dbMap.containsKey(new CBString(str));
      Assert.assertTrue(contains);
    }
  }

  private void testDeleteHalf() throws IOException{
    for(int i = 0; i < numWords;){
      String toDelete = ""+i;
      this.inMemoryMap.remove(toDelete);
      this.dbMap.delete(new CBString(toDelete));
      i += 2;
    }
    Assert.assertEquals(inMemoryMap.size(), dbMap.getNumberOfElements());
  }

  private void testSimpleDelete() throws IOException {
    //First some simple ones
    this.inMemoryMap.remove(""+101);
    this.inMemoryMap.remove(""+201);
    this.inMemoryMap.remove(""+301);
    this.dbMap.delete(new CBString("" + 101));
    this.dbMap.delete(new CBString(""+201));
    this.dbMap.delete(new CBString(""+301));
    Assert.assertFalse(this.dbMap.containsKey(new CBString(""+101)));
    Assert.assertFalse(this.dbMap.containsKey(new CBString(""+201)));
    Assert.assertFalse(this.dbMap.containsKey(new CBString(""+301)));
    Assert.assertEquals(inMemoryMap.size(), dbMap.getNumberOfElements());
  }

  private void testValues() throws IOException {
    for (String str : inMemoryMap.keySet()) {
      Integer inValue = inMemoryMap.get(str);
      Integer dbValue = (Integer) dbMap.search(new CBString(str)).get();
      Assert.assertEquals(inValue, dbValue);
    }
  }

  private void testIterator() {
    /*for (Map.Entry<String, Integer> dbEntry : inMemoryMap.entrySet()) {
      Integer inValue = inMemoryMap.get(dbEntry.getKey());
      Assert.assertNotNull(inValue);
      Assert.assertEquals(dbEntry.getValue(), inValue);

    }*/
  }

}
