package org.mellowtech.core.util;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by msvens on 22/12/15.
 */
public class TGeneratorTest {

  final static String[] testArray = new String[]{
    "AAB", "AAC", "ABA", "ABB", "ABC",
      "ACA", "ACB", "ACC", "BAA", "BAB",
      "BAC", "BBA", "BBB", "BBC", "BCA",
      "BCB", "BCC", "CAA", "CAB", "CAC",
      "CBA", "CBB", "CBC", "CCA", "CCB",
      "CCC"
  };

  @Test
  public void testStringGeneration(){
    Iterator<String> iter = TGenerator.of(String.class, 3, 'A', 'C', false);
    for(int i = 0; i < testArray.length; i++){
      Assert.assertEquals(testArray[i], iter.next());
    }
  }
  @Test
  public void testCharGenerationMutable(){
    Iterator<char[]> iter = TGenerator.of(char[].class, 3, 'A', 'C', true);
    for(int i = 0; i < testArray.length; i++){
      Assert.assertEquals(testArray[i], new String(iter.next()));
    }
  }
  @Test
  public void testCharGeneration(){
    Iterator<char[]> iter = TGenerator.of(char[].class, 3, 'A', 'C', false);
    for(int i = 0; i < testArray.length; i++){
      Assert.assertEquals(testArray[i], new String(iter.next()));
    }
  }
  @Test
  public void testByteGenerationMutable(){
    Iterator<byte[]> iter = TGenerator.of(byte[].class, 3, 'A', 'C', true);
    for(int i = 0; i < testArray.length; i++){
      Assert.assertEquals(testArray[i], new String(iter.next()));
    }
  }
  @Test
  public void testByteGeneration(){
    Iterator<byte[]> iter = TGenerator.of(byte[].class, 3, 'A', 'C', false);
    for(int i = 0; i < testArray.length; i++){
      Assert.assertEquals(testArray[i], new String(iter.next()));
    }
  }
}
