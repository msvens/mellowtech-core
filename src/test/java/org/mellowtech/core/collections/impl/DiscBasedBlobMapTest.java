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
import org.mellowtech.core.collections.BTreeBuilder;
import org.mellowtech.core.collections.DiscMap;
import org.mellowtech.core.collections.SortedDiscMapTemplate;

import java.util.Map;


/**
 * @author Martin Svensson
 */
public class DiscBasedBlobMapTest extends SortedDiscMapTemplate {

  String fName = "discBasedMap";


  @Override
  public DiscMap<String, Integer> reopen() throws Exception {
    return new DiscBasedMap<>(CBString.class, CBInt.class, absPath(fName), true, false);
  }

  @Override
  public Map<String, Integer> init() throws Exception {
    BTreeBuilder builder = new BTreeBuilder();
    builder.maxBlocks(VAL_BLKS).maxIndexBlocks(IDX_BLKS).valueBlockSize(VAL_BLK_SIZE).indexBlockSize(IDX_BLK_SIZE);
    builder.blobValues(true).indexInMemory(true).valuesInMemory(false);
    return new DiscBasedMap<>(CBString.class, CBInt.class, absPath(fName), builder);
  }
}
