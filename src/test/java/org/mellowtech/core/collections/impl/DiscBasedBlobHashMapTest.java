/**
 * DiscBasedMapTest.java, org.mellowtech.core.collections
 *  Copyright Ericsson AB, 2009.
 *
 * The program may be used and/or copied only with the written
 * permission from Ericsson AB, or in accordance with the terms
 * and conditions stipulated in the agreement/contract under
 * which the program has been supplied.
 *
 * All rights reserved.
 */
package org.mellowtech.core.collections.impl;

import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.DiscMap;
import org.mellowtech.core.collections.DiscMapTemplate;

import java.util.Map;

/**
 * @author Martin Svensson
 *
 */
public class DiscBasedBlobHashMapTest extends DiscMapTemplate {


  private String fName = "discBasedHashMap";


  @Override
  public Map<String, Integer> init() throws Exception {
    return new DiscBasedHashMap(CBString.class, CBInt.class,
        absPath(fName), true, false, VAL_BLK_SIZE, VAL_BLKS);
  }

  @Override
  public DiscMap<String, Integer> reopen() throws Exception {
    return new DiscBasedHashMap(CBString.class, CBInt.class, absPath(fName), true, false);
  }




}
