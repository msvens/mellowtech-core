/**
 * 
 */
package org.mellowtech.core.sort;

import java.util.Comparator;

import org.mellowtech.core.bytestorable.BComparable;

/**
 * @author msvens
 *
 */
public class BComparatorArray<A, B extends BComparable <A,B>> implements Comparator<Integer>{

  private final B template;
  private final byte buffer[];
  
  public BComparatorArray(B template, byte[] buffer){
    this.template = template;
    this.buffer = buffer;
  }
  
  @Override
  public int compare(Integer o1, Integer o2) {
    return template.byteCompare(o1, o2, buffer);
  }

}
