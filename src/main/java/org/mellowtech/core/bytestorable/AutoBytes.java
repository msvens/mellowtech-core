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

package org.mellowtech.core.bytestorable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Date: 2013-04-16
 * Time: 21:54
 *
 * @author Martin Svensson
 */
class AutoBytes {
  
  private class MultiField {
    Method get;
    Method set;
    Field field;
    boolean isMethod;
    
    public MultiField(Method get, Method set){
      this.get = get;
      this.set = set;
      isMethod = true;
    }
    
    public MultiField(Field f){
      this.field = f;
      isMethod = false;
    }
  }

  private static class SingletonHolder {
    private final static AutoBytes autoBytes = new AutoBytes();
  }

  private Map<String, TreeMap<Integer, MultiField>> fields;
  //private Map<String, TreeMap<Integer, MethodPair>> methods;

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
    TreeMap <Integer, MultiField> map = new TreeMap<>();

    for(Field f : fs){
      if(f.isAnnotationPresent(BSField.class))
        parseField(f, map);
    }
    
    //methods
    Method[] ms = clazz.getDeclaredMethods();
    for(Method m : ms) {
      if(m.isAnnotationPresent(BSField.class)){
        try{
          parseMethod(clazz, m, ms, map);
        }
        catch(Exception e){
          throw new ByteStorableException(e);
        }
      }
    }
    fields.put(clazz.getName(), map);
  }

  public Set<Integer> getFieldIndexes(Class clazz){
    return this.fields.get(clazz.getName()).keySet();
  }

  public void setField(Class clazz, int index, PrimitiveObject po, Object toSet){
    MultiField mf = fields.get(clazz.getName()).get(index);
    if(mf == null) return;
    try {
      if(mf.isMethod){
        mf.set.invoke(toSet, po.get());
      }
      else {
        mf.field.set(toSet, po.get());
      }
    }
    catch(Exception e){
      throw new ByteStorableException(e);
    }
    
  }

  public Object getField(Class clazz, int index, Object obj){
    MultiField mf = fields.get(clazz.getName()).get(index);
    try{
      return mf.isMethod ? mf.get.invoke(obj) : mf.field.get(obj);
    }
    catch(Exception e){
      throw new ByteStorableException(e);
    }
  }
  
  private void parseMethod(Class clazz, Method m, Method[] ms, TreeMap <Integer, MultiField> map) throws Exception{
    String sib;
    Method set, get;
    if(m.getName().startsWith("get")){
      sib = "set" + m.getName().substring(3);
      get = m;
      set = clazz.getDeclaredMethod(sib, get.getReturnType());
    }
    else if(m.getName().startsWith("set")){
      sib = "get" + m.getName().substring(3);
      set = m;
      get = clazz.getDeclaredMethod(sib);
    }
    else
      return;
    int index = m.getAnnotation(BSField.class).value();
    if(index < 0)
      throw new ByteStorableException("no index of field specified");
    Object fType = get.getReturnType();
    PrimitiveType pt = PrimitiveType.type(fType);
    if(pt == null)
      throw new ByteStorableException("could not instansiate BSAuto");
    get.setAccessible(true);
    set.setAccessible(true);
    map.put(index, new MultiField(get, set));
    
  }
  

  private void parseField(Field f, TreeMap <Integer, MultiField> map){
    f.setAccessible(true);
    int index = f.getAnnotation(BSField.class).value();
    PrimitiveObject po = null;
    if(index < 0)
      throw new ByteStorableException("no index of field specified");


    Object fType = f.getType();
    PrimitiveType pt = PrimitiveType.type(fType);
    if(pt == null)
      throw new ByteStorableException("could not instansiate BSAuto");

    map.put(index, new MultiField(f));
  }
}
