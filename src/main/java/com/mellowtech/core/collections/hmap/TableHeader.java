/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */

package com.mellowtech.core.collections.hmap;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;

/**
 * Date: 2012-10-28
 * Time: 17:29
 *
 * @author Martin Svensson
 */
public class TableHeader extends CBAuto {

  @BSField(index = 1) private Integer tableVersion;
  @BSField(index = 2) private Integer bucketSize;
  @BSField(index = 3) private Integer maxKeyValueSize;
  @BSField(index = 4) private Integer numElements;
  @BSField(index = 5) private Integer dirDepth;

  @BSField(index = 6) private String keyType;
  @BSField(index = 7) private String valueType;

  private int[] directory;

  private ByteStorable kType, vType;


  public int getDirDepth() {
    return dirDepth;
  }

  public void setDirDepth(int dirDepth) {
    this.dirDepth = dirDepth;
  }

  public ByteStorable getKeyType() {
    if(kType == null && keyType != null){
      try{
        kType = (ByteStorable) Class.forName(keyType).newInstance();
      }
      catch(Exception e){
        throw new ByteStorableException(e);
      }
    }
    return kType;
  }

  public void setKeyType(ByteStorable keyType) {
    this.kType = keyType;
    this.keyType = kType.getClass().getName();
  }

  public ByteStorable getValueType(){
    if(vType == null && valueType != null){
      try{
        vType = (ByteStorable) Class.forName(valueType).newInstance();
      }
      catch(Exception e){
        throw new ByteStorableException(e);
      }
    }
    return vType;
  }

  public void setValueType(ByteStorable valueType) {
    this.vType = valueType;
    this.valueType = valueType.getClass().getName();
  }

  public int getBucketSize() {
    return bucketSize;
  }

  public void setBucketSize(int bucketSize) {
    this.bucketSize = bucketSize;
  }

  public int getMaxKeyValueSize() {
    return maxKeyValueSize;
  }

  public void setMaxKeyValueSize(int maxKeyValueSize) {
    this.maxKeyValueSize = maxKeyValueSize;
  }

  public int getNumElements() {
    return numElements;
  }

  public void setNumElements(int numElements) {
    this.numElements = numElements;
  }

  public int[] getDirectory() {
    return directory;
  }

  public void setDirectory(int[] directory) {
    this.directory = directory;
  }

  public int getTableVersion() {
    return tableVersion;
  }

  public void setTableVersion(int tableVersion) {
    this.tableVersion = tableVersion;
  }
}
