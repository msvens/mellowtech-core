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

package org.mellowtech.core.collections.impl;

import org.mellowtech.core.codec.CodecUtil;
import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.collections.KeyValue;

import java.nio.ByteBuffer;

/**
 * @author msvens
 * @since 2017-01-21
 */
public class KeyValueCodec<A,B> implements BCodec<KeyValue<A,B>> {

  final BCodec<A> keyCodec;
  final BCodec<B> valueCodec;

  public KeyValueCodec(BCodec<A> keyCodec, BCodec<B> valueCodec){
    this.keyCodec = keyCodec;
    this.valueCodec = valueCodec;
  }
  @Override
  public int byteSize(KeyValue<A, B> kv) {
    return CodecUtil.byteSize(keyCodec.byteSize(kv.getKey()) + valueCodec.byteSize(kv.getValue()), true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize(bb, true);
  }

  @Override
  public KeyValue<A, B> from(ByteBuffer bb) {
    CodecUtil.getSize(bb, true);
    A key = keyCodec.from(bb);
    B value = valueCodec.from(bb);
    return new KeyValue<>(key,value);
  }

  @Override
  public void to(KeyValue<A, B> kv, ByteBuffer bb) {
    CodecUtil.putSize(keyCodec.byteSize(kv.getKey())+valueCodec.byteSize(kv.getValue()), bb, true);
    keyCodec.to(kv.getKey(), bb);
    valueCodec.to(kv.getValue(), bb);
  }


//  /*public KeyValueCodec(KeyValue<A,B> keyValue){
//    super(keyValue);
//  }
//  /**
//   * Creates a new <code>KeyValue</code> instance.
//   *
//   * @param key
//   *          this pairs key
//   * @param value
//   *          the pairs value
//   */
//  public KeyValueCodec(BComparable<A> key, BStorable<B> value) {
//    this(new KeyValue <A,B> (key,value));
//  }
//
//  public KeyValueCodec(BComparable<A> key){this(key, null);}
//
//
//  /**
//   * Does nothing. Used when reading and writing KeyValues using the implemented
//   * ByteStorable methods.
//   *
//   */
//  public KeyValueCodec() {
//    this(null, null);
//  }
//
//  // *******************STORABLE METHODS:
//  public int byteSize() {
//    return CBUtil.byteSize(get().getKey().byteSize() + get().getValue().byteSize(), true);
//  }
//
//  public int byteSize(ByteBuffer bb) {
//    return CBUtil.peekSize(bb, true);
//  }
//
//  public void to(ByteBuffer bb) {
//    BComparable <?> k = get().getKey();
//    BStorable <?> v = get().getValue();
//    CBUtil.putSize(k.byteSize()+v.byteSize(), bb, true);
//    k.to(bb);
//    v.to(bb);
//  }
//
//  public KeyValueCodec<A,B> from(ByteBuffer bb) {
//    CBUtil.getSize(bb, true);
//    BComparable<A> k = get().getKey().from(bb);
//    BStorable<B> v = get().getValue().from(bb);
//    return new KeyValueCodec<A,B>(k,v);
//  }
//
//  @Override
//  public int compareTo(BComparable<KeyValue<A,B>> other) {
//    return get().compareTo(other.get());
//    //return value.getKey().compareTo(other.get().getKey());
//  }
//
//  /*@Override
//  public int compareTo(KeyValue <A,B> t) throws UnsupportedOperationException {
//    return value.key.compareTo(t.value.key);
//  }*/
//
//  @Override
//  public int hashCode() {
//    return value.getKey().hashCode();
//  }
//
//  @Override
//  public boolean equals(Object obj) {
//    if(obj instanceof KeyValue && obj != null) {
//      KeyValue other = (KeyValue) obj;
//      return value.getKey().equals(other.getKey());
//    }
//    return false;
//  }




}

