/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package org.mellowtech.core.collections.impl;

import org.mellowtech.core.bytestorable.BComparable;
import org.mellowtech.core.bytestorable.BStorable;
import org.mellowtech.core.collections.KeyValue;

/**
 * Return codes for insert and deltion in BPlusTrees. This class should only be
 * used by AbstractBPlusTree and subclasses thereof. Since deletion and
 * insertation are recursive the BPlusReturn gives information on what has
 * previously happened so appropriate action can be taken when returning from
 * the recursion.
 * 
 * @author Martin Svensson
 */

public class BPlusReturn <K extends BComparable <?,K>, V extends BStorable <?,V>> {
  /**
   * Indicates that a block has been split
   */
  protected static int SPLIT = 1000;
  /**
   * Indicates that two blocks has been redistributed
   */
  protected static int REDISTRIBUTE = 2000;
  /**
   * Indicates that two blocks has been merged
   */
  protected static int MERGE = 3000;
  /**
   * No further action has to be taken
   */
  protected static int NONE = 4000;
  /**
   * What action has been carried out...
   */
  protected int action = NONE;
  /**
   * In deletion this holds the key/value that was deleted.
   */
  protected KeyValue <K,V> returnKey = null;
  /**
   * When splitting a block promo holds the Separator that should be promoted
   * (inserted) into the tree.
   */
  protected BTreeKey <K> promo = null;
  /**
   * holds a block number. In search it holds a blocknumber for a newly created
   * block (accordirng to splitting) or in deletion which sibling (either
   * left/right) that was used for redistribution or merging.
   */
  protected int newBlockNo;
  /**
   * A Posion of a key
   */
  protected int keyPos = -1;

  /**
   * Create a new BPlusReturn.
   * 
   * @param action
   *          what action took place
   * @param returnKey
   *          what key to return
   * @param promo
   *          what Separator to promote
   * @param newBlockNo
   *          a block number
   */
  public BPlusReturn(int action, KeyValue <K,V> returnKey, BTreeKey <K> promo,
      int newBlockNo) {
    this.action = action;
    this.returnKey = returnKey;
    this.promo = promo;
    this.newBlockNo = newBlockNo;
  }

  public String toString() {
    return "action: " + action + "\nreturn key: " + returnKey + "\npromo: "
        + promo + "\nnewBlockNo " + newBlockNo + "\nkeyPos: " + keyPos;
  }
}
