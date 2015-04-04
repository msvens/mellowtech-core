/**
 * 
 */
package org.mellowtech.core.collections;

import junit.framework.Assert;

import org.junit.Test;
import org.mellowtech.core.bytestorable.CBBitSet;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.KeyValue;

/**
 * @author msvens
 *
 */
public class KeyValueTest {

  /**
   * 
   */
  @Test
  public void test() {
    KeyValue <CBString, CBInt> kv1 = new KeyValue <> (new CBString("test"), new CBInt(1));
    KeyValue <CBString, CBInt> kv2 = (KeyValue <CBString, CBInt>) kv1.deepCopy();
    
    //System.out.println(kv1.get().key.get());
    Assert.assertEquals(kv1.get().key.get(), kv2.get().key.get());
    Assert.assertEquals(kv1.get().value.get(), kv2.get().value.get());
    
  }

}
