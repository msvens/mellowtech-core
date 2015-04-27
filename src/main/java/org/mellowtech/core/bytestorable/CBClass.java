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
@SuppressWarnings("rawtypes")
public class CBClass extends BStorableImp <Class, CBClass> implements Comparable <CBClass> {

  /**
   * 
   */
  public CBClass() {super(null);}
  
  public CBClass(Class c){super(c);}

  @Override
  public int compareTo(CBClass other) {
    return value.getName().compareTo(other.value.getName());
  }

  @Override
  public CBClass from(ByteBuffer bb) {
    String tmp1 = new CBString().from(bb).get();
    try{
      return new CBClass(Class.forName(tmp1));
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "could not parse header", e);
      return null;
    }
  }

  @Override
  public void to(ByteBuffer bb) {
    CBString store = new CBString(get().getName());
    store.to(bb);
  }

  @Override
  public int byteSize() {
    CBString store = new CBString(get().getName());
    return store.byteSize();
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return new CBString().byteSize(bb);
  }

}
