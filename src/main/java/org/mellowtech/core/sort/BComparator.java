/**
 * 
 */
package org.mellowtech.core.sort;

import java.nio.ByteBuffer;
import java.util.Comparator;

import org.mellowtech.core.bytestorable.BComparable;

/**
 * @author msvens
 *
 */
public class BComparator<A, B extends BComparable <A,B>> implements Comparator<Integer>{

  private final B template;
  private final ByteBuffer buffer;
  public BComparator(B template, ByteBuffer buffer){
    this.template = template;
    this.buffer = buffer;
  }
  
  @Override
  public int compare(Integer o1, Integer o2) {
    return template.byteCompare(o1, o2, buffer);
  }

}
