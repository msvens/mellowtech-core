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

package com.mellowtech.core.collections.mappings;

import com.mellowtech.core.CoreLog;
import com.mellowtech.core.bytestorable.ByteStorable;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Date: 2012-11-03
 * Time: 11:18
 *
 * @author Martin Svensson
 */
public class MappingFactory {

  private Map<String, BSMapping> mapping;

  private static MappingFactory ourInstance = new MappingFactory();



  public static MappingFactory getInstance() {
    return ourInstance;
  }

  public static MappingFactory I(){
    return ourInstance;
  }

  public static BSMapping <?> createMapping(Class <?> c) throws Exception{
    MappingFactory mf = getInstance();
    BSMapping <?> mapping = mf.mapping.get(c.getName());
    if(mapping != null)
      return mapping.getClass().newInstance();
    return null;
  }

  public static ByteStorable <?> newTemplate(Class <?> c) throws Exception{
    return getInstance().mapping.get(c.getName()).getTemplate();
  }

  public static Object fromBytes(Class <?> c, ByteStorable <?> bs) throws Exception{
    return getInstance().mapping.get(c.getName()).fromByteStorable(bs);
  }

  public static ByteStorable <?> toBytes(Object o) throws Exception{
    return getInstance().mapping.get(o.getClass().getName()).toByteStorable(o);
  }

  public static int byteSize(Object o){
    return getInstance().mapping.get(o.getClass().getName()).byteSize(o);
  }

  public void addMapping(Class c, BSMapping mapping){
    this.mapping.put(c.getName(), mapping);
  }

  private MappingFactory() {
    mapping = new HashMap<String, BSMapping> ();
    try{
      addMapping(Boolean.class, new BooleanMapping());
      addMapping(Byte.class, new ByteMapping());
      addMapping(Character.class, new CharacterMapping());
      addMapping(String.class, new StringMapping());
      addMapping(Short.class, new ShortMapping());
      addMapping(Integer.class, new IntegerMapping());
      addMapping(Long.class, new LongMapping());
      addMapping(Float.class, new FloatMapping());
      addMapping(Double.class, new DoubleMapping());
      addMapping(byte[].class, new ByteArrayMapping());
      addMapping(Date.class, new DateMapping());
    }
    catch(Exception e){
      CoreLog.L().log(Level.WARNING, "", e);
    }

  }


}
