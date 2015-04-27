/**
 * 
 */
package org.mellowtech.core.bytestorable;

import org.junit.Before;

/**
 * @author msvens
 *
 */
public class CBByteArrayTest extends BComparableTemplate <byte[], CBByteArray> {

  @Before public void init(){
    type = CBByteArray.class;
    byte[] b1 = new byte[]{1,2};
    byte[] b2 = new byte[]{1,3};
    values = new byte[][]{b1,b2};
    sizes = new int[]{3,3};
    
  }

}
