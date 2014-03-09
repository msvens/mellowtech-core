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

package com.mellowtech.core.bytestorable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Date: 2013-04-16
 * Time: 21:54
 *
 * @author Martin Svensson
 */
class AutoBytes {

  private static class SingletonHolder {
    private final static AutoBytes autoBytes = new AutoBytes();
  }

  private Map<String, TreeMap<Integer, Field>> fields;

  private AutoBytes(){
    this.fields = new HashMap<>();
  }

  public static AutoBytes I(){
    return SingletonHolder.autoBytes;
  }

  public void parseClass(Class clazz){
    if(fields.containsKey(clazz.getName()))
      return;
    Field[] fs = clazz.getDeclaredFields();
    TreeMap <Integer, Field> map = new TreeMap<>();

    for(Field f : fs){
      if(f.isAnnotationPresent(BSField.class))
        parseField(f, map);
    }
    fields.put(clazz.getName(), map);
  }

  public Set<Map.Entry<Integer, Field>> getFieldEntries(Class clazz){
    return this.fields.get(clazz.getName()).entrySet();
  }

  public SortedMap<Integer, Field> getFields(Class clazz){
    return this.fields.get(clazz.getName());
  }

  public Set<Integer> getFieldIndexes(Class clazz){
    return this.fields.get(clazz.getName()).keySet();
  }

  public void setField(Class clazz, int index, PrimitiveObject po, Object toSet){
    Field f = fields.get(clazz.getName()).get(index);
    try{
      f.set(toSet, po.get());
    }
    catch(Exception e){
      throw new ByteStorableException(e);
    }
  }

  public Object getField(Class clazz, int index, Object obj){
    Field f = fields.get(clazz.getName()).get(index);
    try{
      return f.get(obj);
    }
    catch(Exception e){
      throw new ByteStorableException(e);
    }
  }

  private void parseField(Field f, TreeMap <Integer, Field> map){
    f.setAccessible(true);
    int index = f.getAnnotation(BSField.class).index();
    PrimitiveObject po = null;
    if(index < 0)
      throw new ByteStorableException("no index of field specified");


    Object fType = f.getType();
    PrimitiveType pt = PrimitiveType.type(fType);
    if(pt == null)
      throw new ByteStorableException("could not instansiate BSAuto");

    map.put(index, f);
  }
}
