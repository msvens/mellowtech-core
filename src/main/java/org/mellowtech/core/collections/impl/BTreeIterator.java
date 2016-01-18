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

package org.mellowtech.core.collections.impl;

import org.mellowtech.core.CoreLog;
import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.KeyValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>
 * @since 14/01/16.
 */
public class BTreeIterator<A, B extends BComparable<A, B>, C, D extends BStorable<C, D>> implements Iterator<KeyValue<B, D>> {
  Iterator<KeyValue<B, D>> sbIterator;
  ArrayList<Integer> blocks = new ArrayList<>();
  boolean inclusive = true;
  boolean reverse = false;
  boolean endInclusive = true;
  KeyValue<B, D> end = null;
  int currblock = 0;
  KeyValue<B, D> next = null;
  BTreeImp<A, B, C, D> tree;


  public BTreeIterator(BTreeImp<A, B, C, D> tree, boolean reverse, B from, boolean inclusive, B to, boolean endInclusive) {
    this.tree = tree;
    this.inclusive = inclusive;
    this.reverse = reverse;
    this.end = to == null ? null : new KeyValue<>(to, null);
    this.endInclusive = endInclusive;
    initPtrs();
    setCurrentBlock(from);
    nextIter(from);
    getNext();
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public KeyValue<B, D> next() {
    KeyValue<B, D> toRet = next;
    getNext();
    return toRet;
  }

  private void setCurrentBlock(B from) {
    if (reverse) {
      this.currblock = blocks.size() - 1;
      if (from != null) {
        int bNo = tree.searchBlock(from);
        for (; currblock > 0; currblock--) {
          if (blocks.get(currblock) == bNo)
            break;
        }
      }
    } else {
      this.currblock = 0;
      if (from != null) {
        int bNo = tree.searchBlock(from);
        for (; currblock < blocks.size(); currblock++) {
          if (blocks.get(currblock) == bNo)
            break;
        }
      }
    }
  }

  private void getNext() {
    if (sbIterator == null) {
      next = null;
      return;
    }
    KeyValue<B, D> toRet = sbIterator.next();
    if (toRet == null) {
      sbIterator = null;
      next = null;
    } else {
      if (!checkEnd(toRet)) {
        sbIterator = null;
        next = null;
      } else {
        next = toRet;
        if (!sbIterator.hasNext()) {
          nextIter(null);
        }
      }
    }

  }

  private boolean checkEnd(KeyValue<B, D> toCheck) {
    if (end == null) return true;
    int cmp = reverse ? end.compareTo(toCheck) : toCheck.compareTo(end);
    return cmp < 0 || (endInclusive && cmp == 0);
  }

  private void initPtrs() {
    try {
      tree.buildPointers(tree.rootPage, blocks, 0, tree.leafLevel);
    } catch (IOException e) {
      CoreLog.L().log(Level.WARNING, "could not traverse blocks", e);
      throw new Error(e);
    }
  }

  private void nextIter(B from) {
    if (reverse)
      prevBlock(from);
    else
      nextBlock(from);
  }

  private void prevBlock(B from) {
    if (currblock < 0)
      sbIterator = null;
    else {
      try {
        sbIterator = from == null ?
            tree.getValueBlock(blocks.get(currblock)).iterator(true) :
            tree.getValueBlock(blocks.get(currblock)).iterator(true, new KeyValue<>(from, null), inclusive, null, false);
        currblock--;
      } catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "Could not retrieve block", e);
        throw new Error(e);
      }
    }

  }

  private void nextBlock(B from) {
    if (currblock >= blocks.size())
      sbIterator = null;
    else {
      try {
        sbIterator = from == null ?
            tree.getValueBlock(blocks.get(currblock)).iterator() :
            tree.getValueBlock(blocks.get(currblock)).iterator(false, new KeyValue<>(from, null), inclusive, null, false);
        currblock++;
      } catch (IOException e) {
        CoreLog.L().log(Level.WARNING, "Could not retrieve block", e);
        throw new Error(e);
      }
    }
  }
}
