/**
 * 
 */
package com.mellowtech.core.collections;

import org.junit.Test;

import com.mellowtech.core.bytestorable.CBBitSet;

/**
 * @author msvens
 *
 */
public class PointerClassIdTest {

  /**
   * 
   */
  @Test
  public void test() {
    PointerClassId cid = new PointerClassId((short)1, new CBBitSet().toBytes().array());
    PointerClassId cid2 = (PointerClassId) cid.deepCopy();
  }

}
