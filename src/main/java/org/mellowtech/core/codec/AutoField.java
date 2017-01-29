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

package org.mellowtech.core.codec;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class that holds information for fast parsing of
 * BRecord classes. The class is internal to the package
 * and should not be used externally
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
class AutoField {
  
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
    private final static AutoField autoBytes = new AutoField();
  }

  private Map<String, TreeMap<Integer, MultiField>> fields;
  //private Map<String, TreeMap<Integer, MethodPair>> methods;

  private AutoField(){
    this.fields = new HashMap<>();
  }

  public static AutoField I(){
    return SingletonHolder.autoBytes;
  }

  public void parseClass(Class clazz){
    if(fields.containsKey(clazz.getName()))
      return;
    Field[] fs = clazz.getDeclaredFields();
    TreeMap <Integer, MultiField> map = new TreeMap<>();

    for(Field f : fs){
      if(f.isAnnotationPresent(BField.class))
        parseField(f, map);
    }
    
    //methods
    Method[] ms = clazz.getDeclaredMethods();
    for(Method m : ms) {
      if(m.isAnnotationPresent(BField.class)){
        try{
          parseMethod(clazz, m, ms, map);
        }
        catch(Exception e){
          throw new CodecException(e);
        }
      }
    }
    fields.put(clazz.getName(), map);
  }

  public Set<Integer> getFieldIndexes(Class clazz){
    return this.fields.get(clazz.getName()).keySet();
  }

  public void setField(Class clazz, int index, Object o, Object toSet){
    MultiField mf = fields.get(clazz.getName()).get(index);
    if(mf == null) return;
    try {
      if(mf.isMethod){
        mf.set.invoke(toSet, o);
      }
      else {
        mf.field.set(toSet, o);
      }
    }
    catch(Exception e){
      throw new CodecException(e);
    }
    
  }

  public Object getField(Class clazz, int index, Object obj){
    MultiField mf = fields.get(clazz.getName()).get(index);
    try{
      return mf.isMethod ? mf.get.invoke(obj) : mf.field.get(obj);
    }
    catch(Exception e){
      throw new CodecException(e);
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
    int index = m.getAnnotation(BField.class).value();
    if(index < 0)
      throw new CodecException("no index of field specified");
    Object fType = get.getReturnType();
    BCodec pt = Codecs.type(fType);
    if(pt == null)
      throw new CodecException("could not instansiate BSAuto");
    get.setAccessible(true);
    set.setAccessible(true);
    map.put(index, new MultiField(get, set));
    
  }
  

  private void parseField(Field f, TreeMap <Integer, MultiField> map){
    f.setAccessible(true);
    int index = f.getAnnotation(BField.class).value();
    //PrimitiveObject po = null;
    if(index < 0)
      throw new CodecException("no index of field specified");


    Object fType = f.getType();
    BCodec pt = Codecs.type(fType);
    if(pt == null)
      throw new CodecException("could not instansiate BSAuto");

    map.put(index, new MultiField(f));
  }
}
