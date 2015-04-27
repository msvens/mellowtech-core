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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.bytestorable.CBByteArray;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.DiscBasedMap;

import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;


/**
 * @author Martin Svensson
 */
public class BlobMapTest {

  public DiscBasedMap<String, CBString, byte[], CBByteArray> dbMap;
  public TreeMap<String, Integer> inMemoryMap;
  public int numDifferentWords = 1000;
  public int numWords = numDifferentWords * 2;
  String text;
  public static final String dir = "bmtest";
  public static final String name = "blobMap";
  public byte[] bytes;


  @Before
  public void before() throws Exception {
    TestUtils.createTempDir(dir);


    //Generate test data...
    text = "";
    this.inMemoryMap = new TreeMap<>();
    this.dbMap = new DiscBasedMap<>(CBString.class, CBByteArray.class,
            TestUtils.getAbsolutDir(dir+"/"+name), true, false);

    //this.dbMap = new BlobMap<String, byte[]>(new StringMapping(), new ByteArrayMapping(),
    //        TestUtils.getAbsolutDir(dir+"/"+name), true);

    bytes = new byte[100];

    Random r = new Random();
    r.nextBytes(bytes);


  }

  @Test
  public void doTest() throws Exception {
    this.insert();
    this.testContains();
    this.testIterator();
    this.testValue();
    this.testReopen();
  }

  @After
  public void after() throws Exception {

    this.dbMap.save();
    this.dbMap.delete();
    TestUtils.deleteTempDir(dir);

  }

  private void insert() {
    this.dbMap.put("first", bytes);
  }

  private void testContains() {
    /*for (String str : inMemoryMap.keySet()) {
      boolean contains = dbMap.containsKey(str);
      Assert.assertTrue(contains);
    }*/
  }

  private void testValue() {
    byte[] value = this.dbMap.get("first");
    for(int i = 0; i < value.length; i++){
      Assert.assertEquals(bytes[i], value[i]);
    }
  }

  private void testIterator() {
    /*for (Map.Entry<String, Integer> dbEntry : inMemoryMap.entrySet()) {
      Integer inValue = inMemoryMap.get(dbEntry.getKey());
      Assert.assertNotNull(inValue);
      Assert.assertEquals(dbEntry.getValue(), inValue);
    }*/
  }

  private void testReopen() throws Exception {
    this.dbMap.save();

    this.dbMap = new DiscBasedMap<>(CBString.class, CBByteArray.class,
            TestUtils.getAbsolutDir(dir+"/"+name), true, false);

    testValue();
  }


}
