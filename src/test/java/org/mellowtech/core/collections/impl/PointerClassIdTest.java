/**
 * 
 */
package org.mellowtech.core.collections.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.impl.PointerClassId;

/**
 * @author msvens
 *
 */
public class PointerClassIdTest {

  /**
   * 
   */
  @Test
  public void testByteCopy() {
    PointerClassId cid = new PointerClassId((short)1, new CBString("hello").to().array());
    PointerClassId cid2 = cid.deepCopy();
    Assert.assertEquals((short)1, cid2.getClassId());
    Assert.assertEquals("hello", new CBString().from(cid2.get().bytes, 0).get());

  }

  @Test(expected = NullPointerException.class)
  public void testNullBytes() {
    PointerClassId cid = new PointerClassId((short)1, null);
    cid.to();
  }

}
