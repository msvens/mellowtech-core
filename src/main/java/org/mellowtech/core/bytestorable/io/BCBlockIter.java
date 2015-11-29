package org.mellowtech.core.bytestorable.io;

import org.mellowtech.core.bytestorable.BComparable;

import java.util.Iterator;

/**
 * Created by msvens on 28/11/15.
 */
class BCBlockIter<A, B extends BComparable<A, B>> implements Iterator <B> {

  BCBlock<A,B> block;

  int count;
  int cursor = 0;
  int stop = 0;
  int lastRet = -1;


  public BCBlockIter(BCBlock<A,B> block) {
    this(block, null, false, null, false);
  }

  public BCBlockIter(BCBlock <A,B> block, B start, boolean inclusive, B end, boolean endInclusive) {
    this.block = block;
    this.count = block.getNumberOfElements();
    cursor = block.getLowerBound(start, inclusive);
    stop = block.getUpperBound(end, endInclusive);
  }

  @Override
  public boolean hasNext() {return cursor <= stop;}

  @Override
  public B next() {
    if(cursor > stop) return null;
    B key = block.get(cursor);
    lastRet = cursor;
    cursor++;
    return key;
  }

  @Override
  public void remove() {
    if (lastRet == -1)
      throw new IllegalStateException();
    //check();
    block.delete(lastRet);
    stop--;
    cursor--;
    lastRet = -1;
  }

  /*private void check() {
    if (count != high)
      throw new ConcurrentModificationException();
  }*/

}

class BCBlockDescendIter<A, B extends BComparable<A, B>> implements Iterator <B> {

  BCBlock<A,B> block;
  int cursor;
  int lastRet = -1;
  int stop = 0;

  public BCBlockDescendIter(BCBlock<A,B> block){
    this(block, null, false, null, false);
  }

  public BCBlockDescendIter(BCBlock <A,B> block, B start, boolean inclusive, B end, boolean endInclusive){
    this.block = block;
    cursor = block.getUpperBound(start, inclusive);
    stop = block.getLowerBound(end, endInclusive);
  }

  @Override
  public boolean hasNext() {
    return cursor >= stop;
  }

  @Override
  public B next() {
    if(cursor < stop) return null;
    B ret = block.get(cursor);
    lastRet = cursor;
    cursor--;
    return ret;
  }

  @Override
  public void remove() {
    if(lastRet < 0)
      throw new IllegalStateException();
    block.delete(lastRet);
    lastRet = -1;
  }
}