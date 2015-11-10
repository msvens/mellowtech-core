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

import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;

/**
 * @author Martin Svensson
 *
 */
public class DiscBasedHashMapTest extends DiscMapTemplate{


  @Override
  String fName() {
    return "discBasedHashMap";
  }

  @Override
  DiscMap<String, Integer> init(String fileName, int valueBlockSize, int indexBlockSize,
                                int maxValueBlocks, int maxIndexBlocks) throws Exception {

    return new DiscBasedHashMap(CBString.class, CBInt.class,
        fileName, false, false, valueBlockSize, maxValueBlocks);

  }

  @Override
  DiscMap<String, Integer> reopen(String fileName) throws Exception {
    return new DiscBasedHashMap(CBString.class, CBInt.class, fileName, false, false);
  }



  /*
  public DiscBasedHashMap <String, CBString, Integer, CBInt> dbMap;
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

    this.dbMap = new DiscBasedHashMap <> (CBString.class, CBInt.class,
      TestUtils.getAbsolutDir(dir+"/"+name), false, false);
    
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
    this.dbMap = new DiscBasedHashMap <> (CBString.class, CBInt.class,
            TestUtils.getAbsolutDir(dir+"/"+name), false, false);
    Assert.assertEquals(inMemoryMap.size(), dbMap.size());
    for(String str : inMemoryMap.keySet()){
      Integer inValue = inMemoryMap.get(str);
      Integer dbValue = dbMap.get(str);
      Assert.assertEquals(inValue, dbValue);
    }
  }
  */

}
