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

import java.util.*;

/**
 * Date: 2013-02-17
 * Time: 18:07
 *
 * @author Martin Svensson
 */
public enum PrimitiveType {

  StringObject, ByteObject, ShortObject,
  IntObject, LongObject, FloatObject, DoubleObject,
  CharObject, BooleanObject, ByteArrayObject, DateObject,
  ListObject, MapObject, SortedMapObject, BitSetObject, SetObject;

  public static final ByteStorable fromType(PrimitiveType pt){
    switch(pt){
      case StringObject:
        return new CBString();
      case ByteObject:
        return new CBByte();
      case ShortObject:
        return new CBShort();
      case IntObject:
        return new CBInt();
      case LongObject:
        return new CBLong();
      case FloatObject:
        return new CBFloat();
      case DoubleObject:
        return new CBDouble();
      case CharObject:
        return new CBChar();
      case BooleanObject:
        return new CBBoolean();
      case DateObject:
        return new CBDate();
      case ByteArrayObject:
        return new CBByteArray();
      case ListObject:
        return new CBList <> ();
      case MapObject:
        return new CBMap<>();
      case SortedMapObject:
        return new CBSortedMap<>();
      case BitSetObject:
        return new CBBitSet();
      case SetObject:
        return new CBSet <>();
    }
    return null;
  }
  
  public static final ByteStorable get(Object obj) {
    PrimitiveType t = type(obj);
    return fromType(t);
    
  }

  public static final PrimitiveType type(Object obj){
    if(obj instanceof Class)
      return typeClass((Class) obj);
    if(obj instanceof String)
      return PrimitiveType.StringObject;
    else if(obj instanceof Byte)
      return PrimitiveType.ByteObject;
    else if(obj instanceof Short)
      return PrimitiveType.ShortObject;
    else if(obj instanceof Integer)
      return PrimitiveType.IntObject;
    else if(obj instanceof Long)
      return PrimitiveType.LongObject;
    else if(obj instanceof Float)
      return PrimitiveType.FloatObject;
    else if(obj instanceof Double)
      return PrimitiveType.DoubleObject;
    else if(obj instanceof byte[])
      return PrimitiveType.ByteArrayObject;
    else if(obj instanceof Date)
      return PrimitiveType.DateObject;
    else if(obj instanceof Character)
      return PrimitiveType.CharObject;
    else if(obj instanceof Boolean)
      return PrimitiveType.BooleanObject;
    else if(obj instanceof List)
      return PrimitiveType.ListObject;
    else if(obj instanceof SortedMap)
      return PrimitiveType.SortedMapObject;
    else if(obj instanceof Map)
      return PrimitiveType.MapObject;
    else if(obj instanceof BitSet)
      return PrimitiveType.BitSetObject;
    else if(obj instanceof Set)
      return PrimitiveType.SetObject;
    return null;
  }

  public static final PrimitiveType typeClass(Class clazz){
    if(clazz == String.class)
      return PrimitiveType.StringObject;
    else if(clazz == Byte.class || clazz == byte.class)
      return PrimitiveType.ByteObject;
    else if(clazz == Short.class || clazz == short.class)
      return PrimitiveType.ShortObject;
    else if(clazz == Integer.class || clazz == int.class)
      return PrimitiveType.IntObject;
    else if(clazz == Long.class || clazz == long.class)
      return PrimitiveType.LongObject;
    else if(clazz == Float.class || clazz == float.class)
      return PrimitiveType.FloatObject;
    else if(clazz == Double.class || clazz == double.class)
      return PrimitiveType.DoubleObject;
    else if(clazz == byte[].class)
      return PrimitiveType.ByteArrayObject;
    else if(clazz == Date.class)
      return PrimitiveType.DateObject;
    else if(clazz == Character.class || clazz == char.class)
      return PrimitiveType.CharObject;
    else if(clazz == Boolean.class || clazz == boolean.class)
      return PrimitiveType.BooleanObject;
    else if(clazz == ArrayList.class)
      return PrimitiveType.ListObject;
    else if(clazz == TreeMap.class)
      return PrimitiveType.SortedMapObject;
    else if(clazz == HashMap.class)
      return PrimitiveType.MapObject;
    else if(clazz == BitSet.class)
      return PrimitiveType.BitSetObject;
    else if(clazz == Set.class)
      return PrimitiveType.SetObject;
    return null;
  }

  public static final PrimitiveType fromOrdinal(int ordinal){
    if(ordinal == StringObject.ordinal())
      return StringObject;
    else if(ordinal == IntObject.ordinal())
      return IntObject;
    else if(ordinal == ShortObject.ordinal())
      return ShortObject;
    else if(ordinal == LongObject.ordinal())
      return LongObject;
    else if(ordinal == FloatObject.ordinal())
      return FloatObject;
    else if(ordinal == DoubleObject.ordinal())
      return DoubleObject;
    else if(ordinal == ByteArrayObject.ordinal())
      return ByteArrayObject;
    else if(ordinal == DateObject.ordinal())
      return DateObject;
    else if(ordinal == ByteObject.ordinal())
      return ByteObject;
    else if(ordinal == CharObject.ordinal())
      return CharObject;
    else if(ordinal == BooleanObject.ordinal())
      return BooleanObject;
    else if(ordinal == ListObject.ordinal())
      return ListObject;
    else if(ordinal == MapObject.ordinal())
      return MapObject;
    else if(ordinal == SortedMapObject.ordinal())
      return SortedMapObject;
    else if(ordinal == BitSetObject.ordinal())
      return BitSetObject;
    else if(ordinal == SetObject.ordinal())
      return SetObject;
    else
      return null;
  }

  public byte getByte(){
    return (byte) this.ordinal();
  }


}
