/**
 * 
 */
package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;
import java.util.logging.Level;

import org.mellowtech.core.CoreLog;

/**
 * @author msvens
 *
 */
public class CBClass extends ByteStorable <Class> {

  /**
   * 
   */
  public CBClass() {
    // TODO Auto-generated constructor stub
  }
  
  public CBClass(Class c){
    set(c);
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(ByteStorable <Class> other) {
    return this.get().getName().compareTo(other.get().getName());
  }

  @Override
  public ByteStorable <Class> fromBytes(ByteBuffer bb, boolean doNew) {
    CBString tmp1 = new CBString();
    tmp1.fromBytes(bb, false);
    CBClass toRet = doNew ? new CBClass() : this;
    try{
      toRet.set(Class.forName(tmp1.get()));
      return toRet;
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "could not parse header", e);
      return null;
    }
    //return toRet;
  }

  /* (non-Javadoc)
   * @see org.mellowtech.core.bytestorable.ByteStorable#toBytes(java.nio.ByteBuffer)
   */
  @Override
  public void toBytes(ByteBuffer bb) {
    CBString store = new CBString(get().getName());
    store.toBytes(bb);
  }

  @Override
  public int byteSize() {
    CBString store = new CBString(get().getName());
    return store.byteSize();
  }

  /* (non-Javadoc)
   * @see org.mellowtech.core.bytestorable.ByteStorable#byteSize(java.nio.ByteBuffer)
   */
  @Override
  public int byteSize(ByteBuffer bb) {
    return new CBString().byteSize(bb);
  }

}
