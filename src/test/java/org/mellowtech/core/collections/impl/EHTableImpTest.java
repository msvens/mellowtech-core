package org.mellowtech.core.collections.impl;

import org.mellowtech.core.bytestorable.CBInt;
import org.mellowtech.core.bytestorable.CBString;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.BMapTemplate;
import org.mellowtech.core.collections.impl.EHTableImp;

import java.nio.file.Paths;

/**
 * Created by msvens on 05/11/15.
 */
public class EHTableImpTest extends BMapTemplate {

  @Override
  public String fName() {
    return "ehtableimp";
  }

  @Override
  public BMap<String, CBString, Integer, CBInt> init(String fileName, int bucketSize, int maxBuckets) throws Exception{
    return new EHTableImp<>(Paths.get(fileName), CBString.class, CBInt.class, false, bucketSize, maxBuckets);
  }

  @Override
  public BMap<String, CBString, Integer, CBInt> reopen(String fileName) throws Exception{
    return new EHTableImp<>(Paths.get(fileName), CBString.class, CBInt.class, false);
  }
}
