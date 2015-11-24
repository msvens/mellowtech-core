package org.mellowtech.core.collections;

import org.mellowtech.core.bytestorable.*;
import org.mellowtech.core.collections.impl.DiscBasedHashMap;
import org.mellowtech.core.collections.impl.DiscBasedMap;

import java.util.Date;
import java.util.Optional;

/**
 * Created by msvens on 22/10/15.
 */
public class DiscMapBuilder {

  public static int DEFAULT_KEY_BLOCK_SIZE = 1024 * 8;
  public static int DEFAULT_VALUE_BLOCK_SIZE = 1024 * 8;

  private int keyBlockSize = DEFAULT_KEY_BLOCK_SIZE;
  private int valueBlockSize = DEFAULT_VALUE_BLOCK_SIZE;

  private boolean memMappedKeyBlocks = true;
  private boolean memMappedValueBlocks = false;
  private boolean blobValues = false;

  private Optional<Integer> maxKeySize = Optional.empty();
  private Optional<Integer> maxValueSize = Optional.empty();


  public DiscMapBuilder keyBlockSize(int size) {
    this.keyBlockSize = size;
    return this;
  }

  public DiscMapBuilder valueBlockSize(int size) {
    this.valueBlockSize = size;
    return this;
  }

  public DiscMapBuilder memMappedKeyBlocks(boolean memMapped) {
    this.memMappedKeyBlocks = memMapped;
    return this;
  }

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
  public <K, V> DiscMap<K, V> build(Class<K> keyClass, Class<V> valueClass, String fileName, boolean sorted) {
    if (keyClass == Boolean.class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBBoolean.class, CBDate.class, fileName, sorted);
      else throw new Error("unknown value class");
    } else if (keyClass == Byte.class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBByte.class, CBDate.class, fileName, sorted);
      else throw new Error("unknown value class");
    } else if (keyClass == Short.class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBShort.class, CBDate.class, fileName, sorted);
      else throw new Error("unknown value class");
    } else if (keyClass == Integer.class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBInt.class, CBDate.class, fileName, sorted);
      else throw new Error("unknown value class");
    } else if (keyClass == Long.class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBLong.class, CBDate.class, fileName, sorted);
      else throw new Error("unknown value class");
    } else if (keyClass == Float.class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBFloat.class, CBDate.class, fileName, sorted);
      else throw new Error("unknown value class");
    } else if (keyClass == Double.class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBDouble.class, CBDate.class, fileName, sorted);
      else throw new Error("unknown value class");
    } else if (keyClass == String.class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBString.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBString.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBString.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBString.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBString.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBString.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBString.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBString.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBString.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBString.class, CBDate.class, fileName, sorted);
      else throw new Error("unknown value class");
    } else if (keyClass == byte[].class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBByteArray.class, CBDate.class, fileName, sorted);
      else throw new Error("unknonw value class");
    } else if (keyClass == Date.class) {
      if (valueClass == Boolean.class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBBoolean.class, fileName, sorted);
      else if (valueClass == Byte.class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBByte.class, fileName, sorted);
      else if (valueClass == Short.class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBShort.class, fileName, sorted);
      else if (valueClass == Integer.class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBInt.class, fileName, sorted);
      else if (valueClass == Long.class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBLong.class, fileName, sorted);
      else if (valueClass == Float.class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBFloat.class, fileName, sorted);
      else if (valueClass == Double.class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBDouble.class, fileName, sorted);
      else if (valueClass == String.class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBString.class, fileName, sorted);
      else if (valueClass == byte[].class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBByteArray.class, fileName, sorted);
      else if (valueClass == Date.class)
        return (DiscMap<K, V>) this.create(CBDate.class, CBDate.class, fileName, sorted);
      else throw new Error("unknonw value class");
    } else {
      throw new Error("unknown keyType class");
    }
  }


  //Class <B> keyClass, Class <D> valueClass,
  //String fileName, int valueBlockSize, int keyBlockSize, boolean blobValues, boolean keysMemoryMapped, boolean valsMemoryMapped
  public <K, V, BK extends BComparable<K, BK>, BV extends BStorable<V, BV>> DiscMap<K, V> create(Class<BK> keyClass,
                                                                                                 Class<BV> valueClass, String fileName, boolean sorted) {
    try {
      if (calcSize(keyClass.newInstance(), valueClass.newInstance()) * 10 > valueBlockSize)
        blobValues = true;
      if (sorted) {
        return new DiscBasedMap<>(keyClass, valueClass, fileName, valueBlockSize, keyBlockSize, blobValues,
            memMappedKeyBlocks, memMappedValueBlocks);
      } else {
        return new DiscBasedHashMap<>(keyClass, valueClass, fileName, blobValues, memMappedValueBlocks);
      }
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  private int calc(BStorable<?, ?> inst) {
    return inst.isFixed() ? inst.byteSize() : Integer.MAX_VALUE;
  }

  private int calcSize(BStorable<?, ?> key, BStorable<?, ?> value) {
    int keySize = maxKeySize.isPresent() && !key.isFixed() ? maxKeySize.get() : calc(key);
    int valSize = maxValueSize.isPresent() && !value.isFixed() ? maxValueSize.get() : calc(value);
    return keySize + valSize;
  }

  /*
    def calcSize(header: ColumnHeader): ColumnHeader = {
    def size(b: BStorable[_, _], ms: Option[Int]): Int = b.isFixed match {
      case true => b.byteSize
      case false => ms match {
        case Some(s) => s
        case None => Int.MaxValue
      }
    }
    val kSize = size(bctype(header.keyType).newInstance(), header.maxKeySize)
    val vSize = size(bctype(header.valueType).newInstance(), header.maxValueSize)
    header.copy(maxKeySize = Some(kSize), maxValueSize = Some(vSize))
  }
   */


}
