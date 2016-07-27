package org.mellowtech.core.bytestorable;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by msvens on 27/07/16.
 */
public class PrimitiveObjectTest {

  private <A> A createAndCopy(A a){
    PrimitiveObject<A> po = new PrimitiveObject<>(a);
    ByteBuffer bb = po.to();
    bb.flip();
    return new PrimitiveObject<A>().from(bb).get();
  }

  @Test
  public void testBoolean(){
    Boolean v = true;
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testByte(){
    Byte v = 1;
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testShort(){
    Short v = 1;
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testInt(){
    Integer v = Integer.MAX_VALUE;
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testLong(){
    Long v = Long.MAX_VALUE;
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testFloat(){
    Float v = Float.MAX_VALUE;
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testDouble(){
    Double v = Double.MAX_VALUE;
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testChar(){
    Character v = 'a';
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testString(){
    String v = "test";
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testDate(){
    Date v = new Date(System.currentTimeMillis());
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testUUID(){
    UUID v = new UUID(1l,2l);
    Assert.assertEquals(v, createAndCopy(v));
  }

  @Test
  public void testByteArray(){
    byte[] v = "test".getBytes();
    Assert.assertEquals(new String(v), new String(createAndCopy(v)));
  }

  @Test
  public void testList(){
    List<String> l = new LinkedList<>();
    l.add("a");
    l.add("b");
    List<String> ll = createAndCopy(l);
    Assert.assertEquals(l.get(0), ll.get(0));
    Assert.assertEquals(l.get(1), ll.get(1));
  }

  @Test
  public void testMap(){
    Map<Integer,String> m = new HashMap<>();
    m.put(0,"a");
    m.put(1,"b");
    Map<Integer,String> m2 = createAndCopy(m);
    Assert.assertEquals(m.get(0), m2.get(0));
    Assert.assertEquals(m.get(1), m2.get(1));
  }

  @Test
  public void testSortedMap(){
    SortedMap<Integer,String> m = new TreeMap<>();
    m.put(0,"a");
    m.put(1,"b");
    SortedMap<Integer,String> m2 = createAndCopy(m);
    Assert.assertEquals(m.get(0), m2.get(0));
    Assert.assertEquals(m.get(1), m2.get(1));
  }

  @Test
  public void testSet(){
    Set<String> s = new HashSet<>();
    s.add("a");
    s.add("b");
    Set<String> ss = createAndCopy(s);
    Assert.assertTrue(ss.contains("a"));
    Assert.assertTrue(ss.contains("b"));
  }

  @Test
  public void testBitSet(){
    BitSet v = new BitSet();
    v.set(2);
    v.set(128);
    BitSet v1 = createAndCopy(v);
    Assert.assertTrue(v1.get(2));
    Assert.assertTrue(v1.get(128));
  }
}
