/**
 * 
 */
package org.mellowtech.core.bytestorable;

import org.junit.Before;

/**
 * @author msvens
 *
 */
public class CBCharArrayTest extends BComparableTemplate <char[], CBCharArray> {

  @Before public void init(){
    type = CBCharArray.class;
    char[] b1 = new char[]{'a','b'};
    char[] b2 = new char[]{'a','c'};
    values = new char[][]{b1,b2};
    sizes = new int[]{3,3};
    
  }

}
