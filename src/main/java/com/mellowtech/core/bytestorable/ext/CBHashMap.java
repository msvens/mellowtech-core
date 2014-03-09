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
package com.mellowtech.core.bytestorable.ext;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;
import com.mellowtech.core.bytestorable.CBString;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;


/**
 * A ByteStorable Wrapper for a HashMap
 * @author Martin Svensson
 *
 */
@Deprecated
public class CBHashMap extends ByteStorable<CBHashMap> implements Map <ByteStorable <?>, ByteStorable <?>> {
  
  private HashMap <ByteStorable <?>, ByteStorable <?>> map = new HashMap <ByteStorable <?>, ByteStorable <?>> ();
  private CBString keyClass = new CBString();
  private CBString valueClass = new CBString();
  
  
  public CBHashMap(){
  }
  
  public CBHashMap(ByteStorable <?> keyTemplate, ByteStorable <?> valueTemplate){
    this.keyClass = keyTemplate == null ? new CBString() : new CBString(keyTemplate.getClass().getName());
    this.valueClass = valueTemplate == null ? new CBString() : new CBString(valueTemplate.getClass().getName());
  }
  

  
  /******************Overwritten ByteStorable Methods*******************/  
  @Override
  public int byteSize() {
    int size = 8; //size indicator + num elements
    size += keyClass.byteSize();
    size += valueClass.byteSize();
    for(Entry<ByteStorable <?>, ByteStorable <?>> entry : this.entrySet()){
      size += entry.getKey().byteSize();
      size += entry.getValue().byteSize();
    }
    return size;
  }


  @Override
  public int byteSize(ByteBuffer bb) {
    int size = bb.getInt();
    bb.position(bb.position() - 4);
    return size;
  }


  @Override
  public ByteStorable<CBHashMap> fromBytes(ByteBuffer bb, boolean doNew) {
    CBHashMap toRet;
    if(doNew){
      toRet = new CBHashMap();
      toRet.keyClass = this.keyClass;
      toRet.valueClass = this.valueClass;
    }
    else{
      toRet = this;
      toRet.clear();
    }
    bb.getInt(); //byteSize
    int numElems = bb.getInt();
    CBString keyTemplateString = (CBString) new CBString().fromBytes(bb, false);
    CBString valueTemplateString = (CBString) new CBString().fromBytes(bb, false);
    if(keyTemplateString.get().length() > 0)
      toRet.keyClass = keyTemplateString;
    if(valueTemplateString.get().length() > 0)
      toRet.valueClass = valueTemplateString;
    
    //If template information was stored try to instansiate template
    //If it fails use old templates
    try{
      ByteStorable <?> keyTemplate = (ByteStorable <?>) Class.forName(toRet.keyClass.get()).newInstance();
      ByteStorable <?> valueTemplate = (ByteStorable <?>) Class.forName(toRet.valueClass.get()).newInstance();
      for(int i = 0; i < numElems; i++){
        this.put(keyTemplate.fromBytes(bb, true), valueTemplate.fromBytes(bb, true));
      }
      return toRet;
    }
    catch(Exception e){
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  }


  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(this.byteSize());
    bb.putInt(this.size());
    this.keyClass.toBytes(bb);
    this.valueClass.toBytes(bb);
    for(Entry<ByteStorable <?>, ByteStorable <?>> entry : this.entrySet()){
      entry.getKey().toBytes(bb);
      entry.getValue().toBytes(bb);
    }
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    return this.map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return this.map.containsValue(value);
  }

  @Override
  public Set<Entry<ByteStorable<?>, ByteStorable<?>>> entrySet() {
    return this.map.entrySet();
  }

  @Override
  public ByteStorable<?> get(Object key) {
    return this.map.get(key);
  }

  @Override
  public boolean isEmpty() {
    return this.map.isEmpty();
  }

  @Override
  public Set<ByteStorable<?>> keySet() {
    return this.map.keySet();
  }

  @Override
  public ByteStorable<?> put(ByteStorable<?> key, ByteStorable<?> value) {
    return this.map.put(key, value);
  }

  @Override
  public void putAll(Map<? extends ByteStorable<?>, ? extends ByteStorable<?>> m) {
    this.map.putAll(m);
    
  }

  @Override
  public ByteStorable<?> remove(Object key) {
    return this.map.remove(key);
  }

  @Override
  public int size() {
    return this.map.size();
  }

  @Override
  public Collection<ByteStorable<?>> values() {
    return this.map.values();
  }

}
