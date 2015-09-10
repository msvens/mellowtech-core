/**
 * 
 */
package org.mellowtech.core.bytestorable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author msvens
 *
 */
public abstract class BStorableTemplate <A, B extends BStorable<A,B>> {
  
  public Class<B> type;
  public A[] values;
  public int[] sizes;
  
  @Test
  public void testEmptyConstructor() throws Exception{
    type.newInstance();
  }
  
  @Test
  public void testAConstructor() throws Exception{
    Constructor <B> c = type.getConstructor(values[0].getClass());
    c.newInstance(values[0]);
  }
  
  @Test
  public void testCreate() throws Exception {
    newB(0);
  }
  
  @Test
  public void testByteSize() throws Exception{
    B b = newB(0);
    Assert.assertEquals(sizes[0],b.byteSize());
  }
  
  @Test
  public void testByteSizeBuffer() throws Exception{
    B b = newB(0);
    ByteBuffer bb = b.to();
    bb.flip();
    Assert.assertEquals(sizes[0], b.byteSize(bb));
  }
  
  @Test
  public void testDeepCopy() throws Exception {
    B b = newB(0);
    B b1 = b.deepCopy();
    Assert.assertNotNull(b1.get());
  }
  
  @Test
  public void testFromArray() throws Exception {
    B b = newB(0);
    byte array[] = new byte[b.byteSize()];
    b.to(array, 0);
    b.from(array, 0);
    //Assert.assertEquals(values[0], b1.get());
  }
  
  @Test
  public void testFromBuffer() throws Exception {
    B b = newB(0);
    ByteBuffer bb = b.to();
    bb.flip();
    b.from(bb);
    //Assert.assertEquals(values[0], b1.get());
  }
  
  @Test
  public void testFromStream() throws Exception {
    B b = newB(0);
    byte array[] = new byte[b.byteSize()];
    b.to(array,0);
    ByteArrayInputStream stream = new ByteArrayInputStream(array);
    b.from(stream);
    //Assert.assertEquals(values[0], b1.get());
  }
  
  @Test
  public void testFromChannel() throws Exception {
    B b = newB(0);
    byte array[] = new byte[b.byteSize()];
    b.to(array,0);
    ByteArrayInputStream stream = new ByteArrayInputStream(array);
    ReadableByteChannel channel = Channels.newChannel(stream);
    b.from(channel);
    //Assert.assertEquals(values[0], b1.get());
  }
  
  @Test
  public void testGet() throws Exception {
    B b = newB(0);
    Assert.assertEquals(values[0], b.get());
  }
  
  @Test
  public void testTo() throws Exception {
    B b = newB(0);
    ByteBuffer bb = b.to();
    Assert.assertEquals(sizes[0], bb.capacity());
  }
  
  @Test
  public void testToArray() throws Exception {
    B b = newB(0);
    byte array[] = new byte[b.byteSize()];
    b.to(array,0);
  }
  
  @Test
  public void testToStream() throws Exception {
    B b = newB(0);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(b.byteSize());
    b.to(bos);
  }
  
  @Test
  public void testToChannel() throws Exception {
    B b = newB(0);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(b.byteSize());
    WritableByteChannel channel = Channels.newChannel(bos);
    b.to(channel);
  }
  
  public B newB(int index) throws Exception{
    return type.newInstance().create(values[index]);
  }
  
  

}
