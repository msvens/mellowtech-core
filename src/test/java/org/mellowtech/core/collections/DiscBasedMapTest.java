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

import java.io.File;
import java.util.*;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mellowtech.core.TestUtils;
import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.DiscBasedMap;
import org.mellowtech.core.collections.tree.BTreeBuilder;
import org.mellowtech.core.util.Platform;


/**
 * @author Martin Svensson
 */
public class DiscBasedMapTest extends SortedDiscMapTemplate {

  String fName = "discBasedMap";


  @Override
  DiscMap<String, Integer> reopen() throws Exception {
    return new DiscBasedMap<>(CBString.class, CBInt.class, absPath(fName), false, false);
  }

  @Override
  Map<String, Integer> init() throws Exception {
    BTreeBuilder builder = new BTreeBuilder();
    builder.maxBlocks(VAL_BLKS).maxIndexBlocks(IDX_BLKS).valueBlockSize(VAL_BLK_SIZE).indexBlockSize(IDX_BLK_SIZE);
    builder.blobValues(false).indexInMemory(true).valuesInMemory(false);
    return new DiscBasedMap<>(CBString.class, CBInt.class, absPath(fName), builder);
  }
}
