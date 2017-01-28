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
public abstract class BStorableTemplate <A, B extends BStorable<A>> {
  
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
    B b1 = (B) b.deepCopy();
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
    return (B) type.newInstance().create(values[index]);
  }
  
  

}
