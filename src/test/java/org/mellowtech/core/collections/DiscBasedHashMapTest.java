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
package org.mellowtech.core.collections;

import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;

/**
 * @author Martin Svensson
 *
 */
public class DiscBasedHashMapTest extends DiscMapTemplate{


  @Override
  String fName() {
    return "discBasedHashMap";
  }

  @Override
  DiscMap<String, Integer> init(String fileName, int valueBlockSize, int indexBlockSize,
                                int maxValueBlocks, int maxIndexBlocks) throws Exception {

    return new DiscBasedHashMap(CBString.class, CBInt.class,
        fileName, false, false, valueBlockSize, maxValueBlocks);

  }

  @Override
  DiscMap<String, Integer> reopen(String fileName) throws Exception {
    return new DiscBasedHashMap(CBString.class, CBInt.class, fileName, false, false);
  }




}
