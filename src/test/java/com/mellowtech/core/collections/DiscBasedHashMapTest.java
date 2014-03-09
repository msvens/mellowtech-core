/**
 * DiscBasedMapTest.java, com.mellowtech.core.collections
 *  Copyright Ericsson AB, 2009.
 *
 * The program may be used and/or copied only with the written
 * permission from Ericsson AB, or in accordance with the terms
 * and conditions stipulated in the agreement/contract under
 * which the program has been supplied.
 *
 * All rights reserved.
 */
package com.mellowtech.core.collections;

import com.mellowtech.core.TestUtils;
import com.mellowtech.core.collections.mappings.IntegerMapping;
import com.mellowtech.core.collections.mappings.StringMapping;
import com.mellowtech.core.util.Platform;
import de.svenjacobs.loremipsum.LoremIpsum;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Martin Svensson
 *
 */
public class DiscBasedHashMapTest {
  
  public DiscBasedHashMap <String, Integer> dbMap;
  public HashMap<String, Integer> inMemoryMap;
  public int numDifferentWords = 1000;
  public int numWords = numDifferentWords * 2;
  String text;
  public final static String dir = "dbhmtest";
  public final static String name = "discBasedHashMap";
  @Before
  public void before() throws Exception{
    //Create directory for the DB hmap:
    TestUtils.createTempDir(dir);

    
    //Generate test data...
    LoremIpsum li = new LoremIpsum();
    //text = li.getParagraphs(5);
    text = "";
    this.inMemoryMap = new HashMap <String, Integer> ();

    this.dbMap = new DiscBasedHashMap <String, Integer> (new StringMapping(), new IntegerMapping(),
      TestUtils.getAbsolutDir(dir+"/"+name));
    
    Random r = new Random();
    for(int i = 0; i < numWords; i++){
      String word = "" + (int) (r.nextGaussian() * numDifferentWords);
      text += word+ " ";
    }
   
  }
  
  //@Test
  public void doTest() throws Exception{
    this.insert();
    this.testContains();
    this.testValues();
    this.testIterator();
    this.testReopen();
  }

  
  
  @After
  public void after() throws Exception{
    this.dbMap.save();
    this.dbMap.delete();
    TestUtils.deleteTempDir(dir);
  }
  
  private void insert(){
    StringTokenizer st = new StringTokenizer(this.text);
    int tot = 0;
    while(st.hasMoreTokens()){
      tot++;
      String next = st.nextToken();
      Integer count = this.inMemoryMap.get(next);
      if(count == null){
        this.inMemoryMap.put(next, 1);
        this.dbMap.put(next, 1);
      }
      else{
        this.inMemoryMap.put(next, count+1);
        this.dbMap.put(next, count+1);
      }
    }
  }
  
  private void testContains(){
    for(String str : inMemoryMap.keySet()){
      boolean contains = dbMap.containsKey(str);
      Assert.assertTrue(contains);
    }
  }

  private void testValues(){
    for(String str : inMemoryMap.keySet()){
      Integer inValue = inMemoryMap.get(str);
      Integer dbValue = dbMap.get(str);
      Assert.assertEquals(inValue, dbValue);
    }
  }

  private void testIterator(){
    Iterator <Map.Entry <String, Integer>> dbIter = inMemoryMap.entrySet().iterator();
    while(dbIter.hasNext()){
      Map.Entry <String, Integer> dbEntry = dbIter.next();
      Integer inValue = inMemoryMap.get(dbEntry.getKey());
      Assert.assertNotNull(inValue);
      Assert.assertEquals(dbEntry.getValue(), inValue);

    }
  }

  private void testReopen() throws Exception{
    this.dbMap.save();
    this.dbMap = new DiscBasedHashMap <String, Integer> (new StringMapping(), new IntegerMapping(),
            TestUtils.getAbsolutDir(dir+"/"+name));
    Assert.assertEquals(inMemoryMap.size(), dbMap.size());
    for(String str : inMemoryMap.keySet()){
      Integer inValue = inMemoryMap.get(str);
      Integer dbValue = dbMap.get(str);
      Assert.assertEquals(inValue, dbValue);
    }
  }


}
