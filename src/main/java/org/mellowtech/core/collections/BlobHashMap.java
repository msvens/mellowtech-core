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

package org.mellowtech.core.collections;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.ByteStorable;
import org.mellowtech.core.collections.mappings.BSMapping;
import org.mellowtech.core.disc.MapEntry;
import org.mellowtech.core.disc.blockfile.DynamicFile;
import org.mellowtech.core.disc.blockfile.DynamicFilePointer;

/**
 * Date: 2012-10-28
 * Time: 10:19
 * @author Martin Svensson
 */
public class BlobHashMap <K,V> implements DiscMap <K,V> {

  private Map <K,DynamicFilePointer> pointerMap;
  private DynamicFile <V> valueFile;
  private BSMapping <V> valueMapping;

  public BlobHashMap(String fileName, BSMapping <K> keyMapping, BSMapping <V> valueMapping,
                     boolean discKeyMap) throws IOException{
    //this.keyMapping = keyMapping;
    this.valueMapping = valueMapping;
    this.valueFile = new DynamicFile <V> (fileName+"-valueFiles", this.valueMapping.getTemplate());
    if(!discKeyMap){
       pointerMap = new HashMap<K, DynamicFilePointer>() ;
    }
    else{
      pointerMap = new DiscBasedHashMap<K, DynamicFilePointer>(keyMapping, new DynamicFilePointer(),
              fileName);
    }

  }


  @Override
  public void save() throws IOException {
    this.valueFile.flush();
    if(pointerMap instanceof DiscMap){
      ((DiscMap <K, DynamicFilePointer>) pointerMap).save();
    }
  }
  
  @Override
  public void close() throws IOException {
	  this.valueFile.close();
	  if(pointerMap instanceof DiscMap){
	      ((DiscMap <K, DynamicFilePointer>) pointerMap).close();
	    }
  }

  @Override
  public void compact() throws IOException, UnsupportedOperationException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void delete() throws IOException {
    this.valueFile.delete();
    if(pointerMap instanceof DiscMap)
      ((DiscMap <K, DynamicFilePointer>) pointerMap).delete();
    else
      pointerMap.clear();
  }

  @Override
  public Iterator<Entry<K, V>> iterator() {
    return new BlobHashMapIterator();
  }

  @Override
  public Iterator<Entry<K, V>> iterator(K key) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return pointerMap.size();
  }

  @Override
  public boolean isEmpty() {
    return pointerMap.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return pointerMap.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    Iterator<Entry<K, DynamicFilePointer>> iter;

    if(pointerMap instanceof DiscMap){
      iter = ((DiscMap <K, DynamicFilePointer>)pointerMap).iterator();
    }
    else{
      iter = pointerMap.entrySet().iterator();
    }
    while(iter.hasNext()){
      Entry<K, DynamicFilePointer> entry = iter.next();
      try{
        ByteStorable <V> bs = this.valueFile.get(entry.getValue());
        V v = valueMapping.fromByteStorable(bs);
        if(v.equals(value))
          return true;
      } catch (IOException e) {
        CoreLog.L().log(Level.SEVERE, "", e);
        return false;
      }
    }
    return false;
  }

  @Override
  public V get(Object key) {
    return this.getPointerValue(pointerMap.get(key));
  }

  @Override
  public V put(K key, V value) {
    try{
      DynamicFilePointer dfp = pointerMap.get(key);
      if(dfp != null){
        V toRet = this.getPointerValue(dfp);
        dfp = this.valueFile.update(dfp, valueMapping.toByteStorable(value));
        pointerMap.put(key, dfp);
        return toRet;
      }
      else{
        dfp = this.valueFile.insert(valueMapping.toByteStorable(value));
        pointerMap.put(key, dfp);
        return null;
      }
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "", e);
      return null;
    }
   }

  @Override
  public V remove(Object key) {
    try{
    DynamicFilePointer dfp = pointerMap.remove(key);
    if(dfp == null) return null;
    V toRet = this.getPointerValue(dfp);
    this.valueFile.delete(dfp);
      return toRet;
    }
    catch(IOException e){
      CoreLog.L().log(Level.SEVERE, "", e);
    }
    return null;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Entry<? extends K, ? extends V> e : m.entrySet()) {
      this.put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Set<K> keySet() {
    return pointerMap.keySet();
  }

  @Override
  public Collection<V> values() {
    return null;
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    HashSet <Entry<K,V>> toRet = new HashSet<Entry<K, V>>();
    Iterator<Entry<K,V>> iter = this.iterator();
    while(iter.hasNext()){
      toRet.add(iter.next());
    }
    return toRet;
  }

  private V getPointerValue(DynamicFilePointer pointer){
    if(pointer == null) return null;
    try{
      return this.valueMapping.fromByteStorable(this.valueFile.get(pointer));
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "", e);
    }
    return null;
  }

  class BlobHashMapIterator implements Iterator<Entry<K,V>>{

    Iterator <Entry <K, DynamicFilePointer>> iter;

    public BlobHashMapIterator(){
      if(pointerMap instanceof DiscBasedHashMap)
        iter = ((DiscBasedHashMap <K, DynamicFilePointer>) pointerMap).iterator();
      else
        iter = pointerMap.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Entry<K, V> next() {
      Entry <K, DynamicFilePointer> next = iter.next();
      if(next == null) return null;
      MapEntry <K, V> entry = new MapEntry<K, V>();
      entry.setKey(next.getKey());
      if(entry.getValue() != null){
        entry.setValue(getPointerValue(next.getValue()));
      }
      return entry;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
