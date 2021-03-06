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

import java.io.IOException;
import java.util.*;
import org.mellowtech.core.collections.BMap;
import org.mellowtech.core.collections.DiscMap;
import org.mellowtech.core.collections.EHTableBuilder;
import org.mellowtech.core.collections.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Martin Svensson
 * Date: 2012-10-20
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class DiscBasedHashMap <A,B> implements DiscMap<A,B> {

  private BMap<A,B> eht;
  private final Logger logger = LoggerFactory.getLogger(Class.class);

  
  public DiscBasedHashMap(EHTableBuilder <A,B> builder) throws Exception{
    this.eht = builder.build();
  }

  /****************overwritten disc hmap methods******************************/
  public void save() throws IOException{
    eht.save();
  }
  
  public void close() throws IOException{
	  eht.close();
  }

  @Override
  public void compact(){
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete() throws IOException {
    eht.delete();
  }

  @Override
  public Iterator<Entry<A,B>> iterator() {
    return new DiscBasedHashIterator();
  }


  /********************OVERWRITTEN MAP METHODS************************************/
  @Override
  public int size() {
    try{
      return eht.size();
    }
    catch(Exception e){
      logger.error("", e);
      throw new Error(e);
    }
  }

  @Override
  public boolean isEmpty() {
    try {
      return eht.isEmpty();
    }
    catch(Exception e){
      logger.error("", e);
      throw new Error(e);
    }
  }



  @Override
  @SuppressWarnings("unchecked")
  public boolean containsKey(Object key){
    try{
      return eht.containsKey((A) key);
    }
    catch(Exception e){
      logger.error("", e);
    }
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean containsValue(Object value) {
    Iterator <KeyValue<A,B>> iter = eht.iterator();
    B find = (B) value;
    while(iter.hasNext()){
      KeyValue <A,B> kv = iter.next();
      B toComp = kv.getValue();
      if(toComp.equals(find))
        return true;
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public B get(Object key) {
    try{
      return eht.get((A)key);
      /*BStorable<C> ret = eht.get(keyMapping.create((A)key));
      return ret != null ? ret.get() : null;*/
    }
    catch(Exception e){
      logger.error("", e);
    }
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public B put(A key, B value) {
    /*BComparable<A> bsk = keyMapping.create(key);
    BStorable<C> vsk = valueMapping.create(value);*/
    try{
      eht.put(key,value);
    }
    catch(Exception e){
      logger.error("", e);
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public B remove(Object key) {
    //BComparable<A> bs = keyMapping.create((A) key);
    try{
      return eht.remove((A)key);
      /*BStorable<C> v = eht.remove(bs);
      return v != null ? v.get() : null;*/
    }
    catch(IOException e){
      logger.error("", e);
    }
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void putAll(Map<? extends A,? extends B> m) {
    for (Entry<? extends A, ? extends B> e : m.entrySet()) {
      this.put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    try {
      eht.truncate();
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  @Override
  public Set <A> keySet() {
    return new DBKeySet<>(this);
  }

  @Override
  public Collection <B> values() {
    return new DBValueCollection<>(this);
  }

  @Override
  public  Set<Map.Entry<A,B>> entrySet() {
    return new DBEntrySet<>(this);
  }

  private class DiscBasedHashIterator implements Iterator<Entry<A,B>>{

    Iterator <KeyValue <A,B>> iter;

    DiscBasedHashIterator(){
      iter = eht.iterator();
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Entry<A,B> next() {
      KeyValue <A,B> next = iter.next();
      if(next == null) return null;
      return next;
      /*MapEntry <A,C> toRet = new MapEntry <> ();
      toRet.setKey(next.getKey().get());
      if(next.getValue() != null)
        toRet.setValue(next.getValue().get());
      return toRet;*/
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
