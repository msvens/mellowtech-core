/*
 * Copyright 2015 mellowtech.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mellowtech.core.collections;

import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.codec.Codecs;
import org.mellowtech.core.collections.impl.DiscBasedHashMap;
import org.mellowtech.core.collections.impl.DiscBasedMap;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by msvens on 22/10/15.
 */
public class DiscMapBuilder {

  public static int DEFAULT_KEY_BLOCK_SIZE = 1024 * 8;
  public static int DEFAULT_VALUE_BLOCK_SIZE = 1024 * 8;

  private int keyBlockSize = DEFAULT_KEY_BLOCK_SIZE;
  private int valueBlockSize = DEFAULT_VALUE_BLOCK_SIZE;

  //private boolean memMappedKeyBlocks = true;
  private boolean memMappedValueBlocks = false;
  private boolean blobValues = false;

  private Optional<Integer> maxKeySize = Optional.empty();
  private Optional<Integer> maxValueSize = Optional.empty();


  public DiscMapBuilder keyBlockSize(int size) {
    this.keyBlockSize = size;
    return this;
  }

  public DiscMapBuilder blobValues(boolean blobs){
    this.blobValues = blobs;
    return this;
  }

  public DiscMapBuilder valueBlockSize(int size) {
    this.valueBlockSize = size;
    return this;
  }

  /*public DiscMapBuilder memMappedKeyBlocks(boolean memMapped) {
    this.memMappedKeyBlocks = memMapped;
    return this;
  }*/

  public DiscMapBuilder memMappedValueBlocks(boolean memMapped) {
    this.memMappedValueBlocks = memMapped;
    return this;
  }

  public DiscMapBuilder maxKeySize(int size) {
    this.maxKeySize = Optional.of(size);
    return this;
  }

  public DiscMapBuilder maxValueSize(int size) {
    this.maxValueSize = Optional.of(size);
    return this;
  }

  public <K,V> SortedDiscMap<K,V> sorted(Class<K> keyClass, Class<V> valueClass, String fileName) {
    return (SortedDiscMap<K,V>) build(keyClass, valueClass, fileName, true);
  }

  public <K,V> DiscMap<K,V> hashed(Class<K> keyClass, Class<V> valueClass, String fileName) {
    return build(keyClass, valueClass, fileName, false);
  }


  @SuppressWarnings("unchecked")
  /*private <A> BComparable<A> getTemplate(Class<A> clazz){
    if(clazz == Boolean.class)
      return (BComparable<A>) new CBBoolean();
    else if(clazz == Byte.class)
      return (BComparable<A>) new CBByte();
    else if(clazz == Character.class)
      return (BComparable<A>) new CBChar();
    else if(clazz == Short.class)
      return (BComparable<A>) new CBShort();
    else if(clazz == Integer.class)
      return (BComparable<A>) new CBInt();
    else if(clazz == Long.class)
      return (BComparable<A>) new CBLong();
    else if(clazz == Float.class)
      return (BComparable<A>) new CBFloat();
    else if(clazz == Double.class)
      return (BComparable<A>) new CBDouble();
    else if(clazz == String.class)
      return (BComparable<A>) new CBString();
    else if(clazz == byte[].class)
      return (BComparable<A>) new CBByteArray();
    else if(clazz == Date.class)
      return (BComparable<A>) new CBDate();
    else if(clazz == UUID.class)
      return (BComparable<A>) new CBUUID();
    else
      throw new Error("unknown type");
  }*/
  public <A,B> DiscMap<A,B> build(Class<A> keyClass, Class<B> valueClass, String fileName, boolean sorted) {
    return this.create(Codecs.fromClass(keyClass),Codecs.fromClass(valueClass), fileName, sorted);
  }


  public <A,B> DiscMap<A,B> create(BCodec<A> keyClass, BCodec<B> valueClass, String fileName, boolean sorted) {
    try {
      if (calcSize(keyClass, valueClass) * 10 > valueBlockSize)
        blobValues = true;
      if (sorted) {
        BTreeBuilder builder = new BTreeBuilder();
        builder.valueBlockSize(valueBlockSize).valueBlockSize(keyBlockSize).blobValues(blobValues).memoryMappedValues(memMappedValueBlocks);
        return new DiscBasedMap<>(keyClass, valueClass, fileName, builder);
      } else {
        return new DiscBasedHashMap<>(keyClass, valueClass, fileName, blobValues, memMappedValueBlocks);
      }
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  private int calc(BCodec<?> inst) {
    return inst.isFixed() ? inst.fixedSize() : Integer.MAX_VALUE;
  }

  private int calcSize(BCodec<?> key, BCodec<?> value) {
    int keySize = maxKeySize.isPresent() && !key.isFixed() ? maxKeySize.get() : calc(key);
    int valSize = maxValueSize.isPresent() && !value.isFixed() ? maxValueSize.get() : calc(value);
    return keySize + valSize;
  }


}
