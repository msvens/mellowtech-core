/*
 * Copyright (c) 2013 mellowtech.org.
 * 
 * The contents of this file are subject to the terms of one of the following open source licenses:
 * Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the "Licenses"). You can select the
 * license that you prefer but you may not use this file except in compliance with one of these
 * Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and limitations under the
 * Licenses.
 */

package org.mellowtech.core.collections.hmap;
import org.mellowtech.core.bytestorable.*;


/**
 * Date: 2012-10-28 Time: 17:29
 *
 * @author Martin Svensson
 */
public class TableHeader<K extends ByteStorable <?>, V extends ByteStorable <?>> extends CBRecord <TableHeader<K,V>.THRecord> {

  public class THRecord implements AutoRecord {

    @BSField(1)
    private Integer tableVersion;
    @BSField(2)
    private Integer bucketSize;
    @BSField(3)
    private Integer maxKeyValueSize;
    @BSField(4)
    private Integer numElements;
    @BSField(5)
    private Integer dirDepth;
    @BSField(6)
    private String keyType;
    @BSField(7)
    private String valueType;

    public int getTableVersion() {
      return tableVersion;
    }

    public void setTableVersion(int tableVersion) {
      this.tableVersion = tableVersion;
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

    public int getDirDepth() {
      return dirDepth;
    }

    public void setDirDepth(int dirDepth) {
      this.dirDepth = dirDepth;
    }
  }

  private int[] directory;

  private K kType;
  private V vType;

  public TableHeader() {
    super();
  }


  public K getKeyType() {
    if (kType == null && record.keyType != null) {
      try {
        kType = (K) Class.forName(record.keyType).newInstance();
      } catch (Exception e) {
        throw new ByteStorableException(e);
      }
    }
    return kType;
  }

  public void setKeyType(K keyType) {
    this.kType = keyType;
    this.record.keyType = kType.getClass().getName();
  }

  public V getValueType() {
    if (vType == null && record.valueType != null) {
      try {
        vType = (V) Class.forName(record.valueType).newInstance();
      } catch (Exception e) {
        throw new ByteStorableException(e);
      }
    }
    return vType;
  }

  public void setValueType(V valueType) {
    this.vType = valueType;
    this.record.valueType = valueType.getClass().getName();
  }



  public int[] getDirectory() {
    return directory;
  }

  public void setDirectory(int[] directory) {
    this.directory = directory;
  }

  @Override
  protected THRecord newT() {
    return new THRecord();
  }


}
