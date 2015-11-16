package org.mellowtech.core.collections;

import junit.framework.Assert;
import org.junit.Test;
import org.mellowtech.core.util.MapEntry;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.SortedMap;

/**
 * Created by msvens on 11/11/15.
 */
public abstract class SortedDiscMapTemplate extends DiscMapTemplate {

  SortedDiscMap <String, Integer> sdm;

  abstract SortedDiscMap <String, Integer> initMap(String fileName, int valueBlockSize,
                                                   int indexBlockSize, int maxValueBlocks, int maxIndexBlocks) throws Exception;
  public DiscMap<String,Integer>
  init(String fileName, int valueBlockSize, int indexBlockSize, int maxValueBlocks, int maxIndexBlocks) throws Exception{
    sdm = initMap(fileName, valueBlockSize, indexBlockSize, maxValueBlocks, maxIndexBlocks);
    return sdm;
  }

  abstract SortedDiscMap <String, Integer> reopenMap(String fileName) throws Exception;

  public DiscMap<String,Integer> reopen(String fileName) throws Exception{
    sdm = reopenMap(fileName);
    return sdm;
  }

  @Test
  public void zeroIteratorFrom() throws IOException {
    Assert.assertFalse(sdm.iterator(firstWord).hasNext());
  }

  @Test
  public void zeroCeilingEntry() throws IOException {
    Assert.assertNull(sdm.ceilingEntry(firstWord));
  }

  @Test
  public void zeroCeilingKey() throws IOException {
    Assert.assertNull(sdm.ceilingKey(firstWord));
  }

  @Test
  public void zeroDescendingKeySet() throws IOException {
    Assert.assertTrue(sdm.descendingKeySet().isEmpty());
  }

  @Test
  public void zeroDescendingMap() throws IOException {
    Assert.assertTrue(sdm.descendingMap().isEmpty());
  }

  @Test
  public void zeroFirstEntry() throws IOException {
    Assert.assertNull(sdm.firstEntry());
  }

  @Test
  public void zeroFloorEntry() throws IOException {
    Assert.assertNull(sdm.floorEntry(firstWord));
  }

  @Test
  public void zeroFloorKey() throws IOException {
    Assert.assertNull(sdm.floorKey(firstWord));
  }

  @Test
  public void zeroHeadMap() throws IOException {
    SortedMap<String,Integer> hm = sdm.headMap(forthWord);
    Assert.assertTrue(sdm.headMap(forthWord).isEmpty());
  }

  @Test
  public void zeroHeadMapInclusive() throws IOException {
    Assert.assertTrue(sdm.headMap(firstWord, true).isEmpty());
  }

  @Test
  public void zeroHigherEntry() throws IOException {
    Assert.assertNull(sdm.higherEntry(firstWord));
  }

  @Test
  public void zeroHigherKey() throws IOException {
    Assert.assertNull(sdm.higherKey(firstWord));
  }

  @Test
  public void zeroLastEntry() throws IOException {
    Assert.assertNull(sdm.lastEntry());
  }

  @Test
  public void zeroLowerEntry() throws IOException {
    Assert.assertNull(sdm.lowerEntry(forthWord));
  }

  @Test
  public void zeroLowerKey() throws IOException {
    Assert.assertNull(sdm.lowerKey(forthWord));
  }

  @Test
  public void zeroNavigableKeySet() throws IOException {
    Assert.assertTrue(sdm.navigableKeySet().isEmpty());
  }

  @Test
  public void zeroPollFirstEntry() throws IOException {
    Assert.assertNull(sdm.pollFirstEntry());
  }

  @Test
  public void zeroPollLastEntry() throws IOException {
    Assert.assertNull(sdm.pollLastEntry());
  }

  @Test
  public void zeroSubMap() throws IOException {
    Assert.assertTrue(sdm.subMap(firstWord, forthWord).isEmpty());
  }

  @Test
  public void zeroSubMapInclusive() throws IOException {
    Assert.assertTrue(sdm.subMap(firstWord, true, forthWord, true).isEmpty());
  }

  @Test
  public void zeroTailMap() throws IOException {
    Assert.assertTrue(sdm.tailMap(firstWord).isEmpty());
  }

  @Test
  public void zeroTailMapInclusive() throws IOException {
    Assert.assertTrue(sdm.tailMap(firstWord, true).isEmpty());
  }

  //Test 1 element
  @Test
  public void oneIteratorFrom() throws IOException {
    onePut();
    Assert.assertTrue(sdm.iterator(firstWord).hasNext());
  }

  @Test
  public void oneCeilingEntry() throws IOException {
    onePut();
    Assert.assertEquals(new MapEntry<>(firstWord, firstWord.length()), sdm.ceilingEntry(firstWord));
  }

  @Test
  public void oneCeilingKey() throws IOException {
    onePut();
    Assert.assertEquals(firstWord, sdm.ceilingKey(firstWord));
  }

  @Test
  public void oneDescendingKeySet() throws IOException {
    onePut();
    Assert.assertEquals(1, sdm.descendingKeySet().size());
  }

  @Test
  public void oneDescendingMap() throws IOException {
    onePut();
    Assert.assertEquals(1, sdm.descendingMap().size());
  }

  @Test
  public void oneFirstEntry() throws IOException {
    onePut();
    Assert.assertEquals(firstWord, sdm.firstEntry().getKey());
  }

  @Test
  public void oneFloorEntry() throws IOException {
    onePut();
    Assert.assertEquals(firstWord, sdm.floorEntry(firstWord).getKey());
  }

  @Test
  public void oneFloorKey() throws IOException {
    onePut();
    Assert.assertEquals(firstWord, sdm.floorKey(firstWord));
  }

  @Test
  public void oneHeadMap() throws IOException {
    onePut();
    SortedMap<String,Integer> hm = sdm.headMap(firstWord);
    Assert.assertEquals(0, hm.size());
  }

  @Test
  public void oneHeadMapInclusive() throws IOException {
    onePut();
    Assert.assertEquals(1, sdm.headMap(firstWord, true).size());
  }

  @Test
  public void oneHigherEntry() throws IOException {
    onePut();
    Assert.assertNull(sdm.higherEntry(firstWord));
  }

  @Test
  public void oneHigherKey() throws IOException {
    onePut();
    Assert.assertNull(sdm.higherKey(firstWord));
  }

  @Test
  public void oneLastEntry() throws IOException {
    onePut();
    Assert.assertEquals(firstWord, sdm.lastEntry().getKey());
  }

  @Test
  public void oneLowerEntry() throws IOException {
    onePut();
    Assert.assertNull(sdm.lowerEntry(firstWord));
  }

  @Test
  public void oneLowerKey() throws IOException {
    onePut();
    Assert.assertNull(sdm.lowerKey(firstWord));
  }

  @Test
  public void oneNavigableKeySet() throws IOException {
    onePut();
    Assert.assertEquals(1, sdm.navigableKeySet().size());
  }

  @Test
  public void onePollFirstEntry() throws IOException {
    onePut();
    Assert.assertNotNull(sdm.pollFirstEntry());
    Assert.assertEquals(0, sdm.size());
  }

  @Test
  public void onePollLastEntry() throws IOException {
    onePut();
    Assert.assertNotNull(sdm.pollLastEntry());
    Assert.assertEquals(0, sdm.size());
  }

  @Test
  public void oneSubMap() throws IOException {
    onePut();
    Assert.assertFalse(sdm.subMap(firstWord, forthWord).isEmpty());
  }

  @Test
  public void oneSubMapInclusive() throws IOException {
    onePut();
    Assert.assertFalse(sdm.subMap(firstWord, true, forthWord, true).isEmpty());
  }

  @Test
  public void oneTailMap() throws IOException {
    onePut();
    Assert.assertEquals(1, sdm.tailMap(firstWord).size());
  }

  @Test
  public void oneTailMapExclusive() throws IOException {
    onePut();
    Assert.assertEquals(0, sdm.tailMap(firstWord, false).size());
  }


}
