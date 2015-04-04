/**
 * 
 */
package org.mellowtech.core.collections;

import org.junit.Test;
import org.mellowtech.core.bytestorable.CBBitSet;
import org.mellowtech.core.collections.PointerClassId;

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
