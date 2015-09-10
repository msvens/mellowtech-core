/**
 * 
 */
package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author msvens
 *
 */
public abstract class BComparableTemplate <A, B extends BComparable<A,B>> 
  extends BStorableTemplate <A,B>{
   
  @Test
  public void testEquals() throws Exception {
    B b = newB(0);
    B b1 = b.deepCopy();
    Assert.assertEquals(b, b1);
  }
  
  @Test
  public void testCompareToTSame() throws Exception {
    B b = newB(0);
    B b1 = b.deepCopy();
    Assert.assertEquals(0, b.compareTo(b1));
  }
  
  @Test
  public void testCompareToLess() throws Exception {
    B b = newB(0);
    B b1 = newB(1);
    Assert.assertTrue(b.compareTo(b1) < 0);
  }
  
  @Test
  public void testByteCompareSameIBIBB() throws Exception {
    B tmp = newB(0);
    byte[] b = newB(0).to().array();
    ByteBuffer b1 = newB(0).to();
    Assert.assertEquals(0, tmp.byteCompare(0, b, 0, b1));
  }
  
  @Test
  public void testByteCompareLessIBIBB() throws Exception {
    B tmp = newB(0);
    byte[] b = newB(0).to().array();
    ByteBuffer b1 = newB(1).to();
    Assert.assertTrue(tmp.byteCompare(0, b, 0, b1) < 0);
  }
  
  @Test
  public void testByteCompareSameIBBIB() throws Exception {
    B tmp = newB(0);
    ByteBuffer b = newB(0).to();
    byte[] b1 = newB(0).to().array();
    Assert.assertEquals(0, tmp.byteCompare(0, b, 0, b1));
  }
  
  @Test
  public void testByteCompareLessIBBIB() throws Exception {
    B tmp = newB(0);
    ByteBuffer b = newB(0).to();
    byte[] b1 = newB(1).to().array();
    Assert.assertTrue(tmp.byteCompare(0, b, 0, b1) < 0);
  }
  
  @Test
  public void testByteCompareSameIBBIBB() throws Exception {
    B tmp = newB(0);
    ByteBuffer b = newB(0).to();
    ByteBuffer b1 = newB(0).to();
    Assert.assertEquals(0, tmp.byteCompare(0, b, 0, b1));
  }
  
  @Test
  public void testByteCompareLessIBBIBB() throws Exception {
    B tmp = newB(0);
    ByteBuffer b = newB(0).to();
    ByteBuffer b1 = newB(1).to();
    Assert.assertTrue(tmp.byteCompare(0, b, 0, b1) < 0);
  }
  
  @Test
  public void testByteCompareSameIIB() throws Exception {
    B tmp = newB(0);
    ByteBuffer tmpBuff = ByteBuffer.allocate(sizes[0]*2);
    newB(0).to(tmpBuff);
    newB(0).to(tmpBuff);
    Assert.assertEquals(0, tmp.byteCompare(0, sizes[0], tmpBuff.array()));
  }
  
  @Test
  public void testByteCompareLessIIB() throws Exception {
    B tmp = newB(0);
    ByteBuffer tmpBuff = ByteBuffer.allocate(sizes[0]*2);
    newB(0).to(tmpBuff);
    newB(1).to(tmpBuff);
    Assert.assertTrue(tmp.byteCompare(0, sizes[0], tmpBuff.array()) < 0);
  }
  
  @Test
  public void testByteCompareSameIIBB() throws Exception {
    B tmp = newB(0);
    ByteBuffer tmpBuff = ByteBuffer.allocate(sizes[0]*2);
    newB(0).to(tmpBuff);
    newB(0).to(tmpBuff);
    Assert.assertEquals(0, tmp.byteCompare(0, sizes[0], tmpBuff));
  }
  
  @Test
  public void testByteCompareLessIIBB() throws Exception {
    B tmp = newB(0);
    ByteBuffer tmpBuff = ByteBuffer.allocate(sizes[0]*2);
    newB(0).to(tmpBuff);
    newB(1).to(tmpBuff);
    Assert.assertTrue(tmp.byteCompare(0, sizes[0], tmpBuff) < 0);
  }
  
  
  
  public B newB(int index) throws Exception{
    return type.newInstance().create(values[index]);
  }
  
  

}
