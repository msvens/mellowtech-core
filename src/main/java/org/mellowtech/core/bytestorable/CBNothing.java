package org.mellowtech.core.bytestorable;

import java.nio.ByteBuffer;

/**
 * Created by msvens on 01/11/15.
 */
public final class CBNothing implements BStorable<Object, CBNothing> {

  @Override
  public Object get() {
    return null;
  }

  @Override
  public CBNothing from(ByteBuffer bb) {
    return this;
  }

  @Override
  public void to(ByteBuffer bb) {
    ;
  }

  @Override
  public int byteSize() {
    return 0;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return 0;
  }

  @Override
  public boolean isFixed() {
    return true;
  }


}
