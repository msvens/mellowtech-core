package org.mellowtech.core.collections.hmap;

import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.impl.EHTableImp;

import java.nio.file.Paths;

/**
 * Created by msvens on 05/11/15.
 */
public class EHTableImpTest extends BMapTemplate{

  @Override
  String fName() {
    return "ehtableimp";
  }

  @Override
  BMap<String, CBString, Integer, CBInt> init(String fileName, int bucketSize, int maxBuckets) throws Exception{
    return new EHTableImp<>(Paths.get(fileName), CBString.class, CBInt.class, false, bucketSize, maxBuckets);
  }

  @Override
  BMap<String, CBString, Integer, CBInt> reopen(String fileName) throws Exception{
    return new EHTableImp<>(Paths.get(fileName), CBString.class, CBInt.class, false);
  }
}
