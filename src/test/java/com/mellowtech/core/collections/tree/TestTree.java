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
import com.mellowtech.core.collections.KeyValue;
import junit.framework.Assert;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Date: 2013-03-22
 * Time: 16:30
 *
 * @author Martin Svensson
 */
public class TestTree {

  public BTree<CBString, CBInt> dbMap;
  public TreeMap<String, Integer> inMemoryMap;
  public int numDifferentWords = 10000;
  public int numWords = numDifferentWords * 1;
  String text;

  public TestTree(BTree <CBString, CBInt> dbMap){

    this.dbMap = dbMap;
    this.inMemoryMap = new TreeMap<>();
    text = "";
    Random r = new Random();
    for (int i = 0; i < numWords; i++) {

      String word = "" + i;
      text += word + " ";
    }
  }

  public void setDbMap(BTree <CBString, CBInt> dbMap){
    this.dbMap = dbMap;
  }

  protected void insert() throws IOException {
    StringTokenizer st = new StringTokenizer(this.text);
    int tot = 0;
    while (st.hasMoreTokens()) {
      tot++;
      String next = st.nextToken();
      Integer count = this.inMemoryMap.get(next);
      if (count == null) {
        this.inMemoryMap.put(next, Integer.parseInt(next));
        this.dbMap.put(new CBString(next), new CBInt(Integer.parseInt(next)));
      } else {
        this.inMemoryMap.put(next, count + 1);
        this.dbMap.put(new CBString(next), new CBInt(count + 1));
      }
    }
  }

  protected void testDeleteHalf() throws IOException{
    for(int i = 0; i < numWords;){
      String toDelete = ""+i;
      Assert.assertTrue(inMemoryMap.containsKey(toDelete));
      //testConsistency();
      //Assert.assertTrue(dbMap.containsKey(new CBString(toDelete)));
      this.inMemoryMap.remove(toDelete);
      this.dbMap.remove(new CBString(toDelete));
      i += 2;
    }
    Assert.assertEquals(inMemoryMap.size(), dbMap.size());
  }

  protected void testSize() throws IOException{
    Assert.assertEquals(inMemoryMap.size(), dbMap.size());
  }

  protected void testSimpleDelete() throws IOException {
    //First some simple ones
    this.inMemoryMap.remove(""+101);
    this.inMemoryMap.remove(""+201);
    this.inMemoryMap.remove(""+301);
    this.dbMap.remove(new CBString("" + 101));
    this.dbMap.remove(new CBString("" + 201));
    this.dbMap.remove(new CBString("" + 301));
    Assert.assertFalse(this.dbMap.containsKey(new CBString(""+101)));
    Assert.assertFalse(this.dbMap.containsKey(new CBString(""+201)));
    Assert.assertFalse(this.dbMap.containsKey(new CBString(""+301)));
    Assert.assertEquals(inMemoryMap.size(), dbMap.size());
  }

  protected void testContains() throws IOException{
    for (String str : inMemoryMap.keySet()) {
      boolean contains = dbMap.containsKey(new CBString(str));
      Assert.assertTrue(contains);
    }
  }

  protected boolean confirmValues(){
    try{
      for (String str : inMemoryMap.keySet()) {
        Integer inValue = inMemoryMap.get(str);
        Integer dbValue = dbMap.get(new CBString(str)).get();
        if(dbValue == null) return false;
      }
      return true;
    }
    catch(Exception e){e.printStackTrace(); return false;}
  }

  protected void testValues() throws IOException {
    for (String str : inMemoryMap.keySet()) {
      Integer inValue = inMemoryMap.get(str);
      Integer dbValue = dbMap.get(new CBString(str)).get();
      Assert.assertEquals(inValue, dbValue);
    }
  }

  protected void testIterator() {
    Iterator<KeyValue<CBString, CBInt>> iter = dbMap.iterator();
    int i = 0;
    String currKey = "";
    while(iter.hasNext()){
      i++;
      String key = iter.next().getKey().get();
      Assert.assertTrue(key.compareTo(currKey) > 0);
      currKey = key;
      Assert.assertTrue(inMemoryMap.containsKey(key));
    }
    Assert.assertEquals(inMemoryMap.size(), i);
  }

  protected void testDeleteAll() throws Exception {
    for (String str : inMemoryMap.keySet()) {
      Integer inValue = inMemoryMap.get(str);
      Integer dbValue = dbMap.remove(new CBString(str)).get();
      Assert.assertEquals(inValue, dbValue);
    }
    inMemoryMap.clear();
    Assert.assertEquals(inMemoryMap.size(), dbMap.size());
  }
}
