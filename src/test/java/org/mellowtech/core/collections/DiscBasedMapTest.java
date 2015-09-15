/**
 * DiscBasedMapTest.java, org.mellowtech.core.collections
 *  Copyright Ericsson AB, 2009.
 *
 * The program may be used and/or copied only with the written
 * permission from Ericsson AB, or in accordance with the terms
 * and conditions stipulated in the agreement/contract under
 * which the program has been supplied.
 *
 * All rights reserved.
 */
package org.mellowtech.core.collections;

import java.io.File;
import java.util.*;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.DiscBasedMap;
import org.mellowtech.core.util.Platform;


/**
 * @author Martin Svensson
 */
public class DiscBasedMapTest {

  public DiscBasedMap<String, CBString, Integer, CBInt> dbMap;
  public TreeMap<String, Integer> inMemoryMap;
  public int numDifferentWords = 1000;
  public int numWords = numDifferentWords * 2;
  String text;
  public static final String dir = "dbmtest";
  public static final String name = "discBasedMap";


  @Before
  public void before() throws Exception {
    TestUtils.createTempDir(dir);


    //Generate test data...
    text = "";
    this.inMemoryMap = new TreeMap<>();

    this.dbMap = new DiscBasedMap<>(CBString.class, CBInt.class,
            TestUtils.getAbsolutDir(dir+"/"+name), false, false);

    Random r = new Random();
    for (int i = 0; i < numWords; i++) {

      String word = "" + (int) (r.nextGaussian() * numDifferentWords);
      text += word + " ";
    }

  }

  @Test
  public void doTest() throws Exception {
    this.insert();
    this.testContains();
    this.testFirstKey();
    this.testLastKey();
    this.testIterator();
    this.testValues();
    this.testReopen();
  }

  @After
  public void after() throws Exception {

    this.dbMap.save();
    this.dbMap.delete();
    TestUtils.deleteTempDir(dir);

  }

  private void insert() {
    StringTokenizer st = new StringTokenizer(this.text);
    int tot = 0;
    while (st.hasMoreTokens()) {
      tot++;
      String next = st.nextToken();
      Integer count = this.inMemoryMap.get(next);
      if (count == null) {
        this.inMemoryMap.put(next, 1);
        this.dbMap.put(next, 1);
      } else {
        this.inMemoryMap.put(next, count + 1);
        this.dbMap.put(next, count + 1);
      }
    }
  }

  private void testContains() {
    for (String str : inMemoryMap.keySet()) {
      boolean contains = dbMap.containsKey(str);
      Assert.assertTrue(contains);
    }
  }

  private void testFirstKey() {
    Assert.assertEquals(inMemoryMap.firstKey(), dbMap.firstKey());
  }

  private void testLastKey() {
    Assert.assertEquals(inMemoryMap.lastKey(), dbMap.lastKey());
  }

  private void testValues() {
    for (String str : inMemoryMap.keySet()) {
      Integer inValue = inMemoryMap.get(str);
      Integer dbValue = dbMap.get(str);
      Assert.assertEquals(inValue, dbValue);
    }
  }

  private void testIterator() {
    for (Map.Entry<String, Integer> dbEntry : inMemoryMap.entrySet()) {
      Integer inValue = inMemoryMap.get(dbEntry.getKey());
      Assert.assertNotNull(inValue);
      Assert.assertEquals(dbEntry.getValue(), inValue);

    }
  }

  private void testReopen() throws Exception {
    this.dbMap.save();

    this.dbMap = new DiscBasedMap<>(CBString.class, CBInt.class,
            TestUtils.getAbsolutDir(dir+"/"+name), false, false);
    Assert.assertEquals(inMemoryMap.size(), dbMap.size());
    for (String str : inMemoryMap.keySet()) {
      Integer inValue = inMemoryMap.get(str);
      Integer dbValue = dbMap.get(str);
      Assert.assertEquals(inValue, dbValue);
    }
  }


}
