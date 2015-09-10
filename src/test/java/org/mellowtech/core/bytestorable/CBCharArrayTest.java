/**
 * 
 */
package org.mellowtech.core.bytestorable;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

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
