/**
 * 
 */
package com.mellowtech.core.collections;

import junit.framework.Assert;

import org.junit.Test;

import com.mellowtech.core.bytestorable.CBBitSet;
import com.mellowtech.core.bytestorable.CBInt;
import com.mellowtech.core.bytestorable.CBString;

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
