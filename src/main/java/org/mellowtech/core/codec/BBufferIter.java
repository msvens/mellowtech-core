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

package org.mellowtech.core.codec;

import java.util.Iterator;


/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
class BBufferIter<A extends Comparable<? super A>> implements Iterator<A> {

  BBuffer<A> block;

  int count;
  int cursor = 0;
  int stop = 0;
  int lastRet = -1;


  public BBufferIter(BBuffer<A> block) {
    this(block, null, false, null, false);
  }

  public BBufferIter(BBuffer<A> block, A start, boolean inclusive, A end, boolean endInclusive) {
    this.block = block;
    this.count = block.getNumberOfElements();
    cursor = block.getLowerBound(start, inclusive);
    stop = block.getUpperBound(end, endInclusive);
  }

  @Override
  public boolean hasNext() {return cursor <= stop;}

  @Override
  public A next() {
    if(cursor > stop) return null;
    A key = block.get(cursor);
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
}

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 */
class BBufferDescendIter<A extends Comparable<? super A>> implements Iterator <A> {

  BBuffer<A> block;
  int cursor;
  int lastRet = -1;
  int stop = 0;

  public BBufferDescendIter(BBuffer<A> block){
    this(block, null, false, null, false);
  }

  public BBufferDescendIter(BBuffer<A> block, A start, boolean inclusive,
                            A end, boolean endInclusive){
    this.block = block;
    cursor = block.getUpperBound(start, inclusive);
    stop = block.getLowerBound(end, endInclusive);
  }

  @Override
  public boolean hasNext() {
    return cursor >= stop;
  }

  @Override
  public A next() {
    if(cursor < stop) return null;
    A ret = block.get(cursor);
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
