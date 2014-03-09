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

package com.mellowtech.core.io;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * A file that that stores byte records. Implementations of this class is free
 * to use any number of underlying files
 *
 * Date: 2013-03-11
 * Time: 07:55
 *
 * @author Martin Svensson
 */
public interface RecordFile {


  public Map<Integer, Integer> compact() throws IOException;
  public boolean save() throws IOException;
  public void close() throws IOException;
  public void clear() throws IOException;


  /**
   * Get the number of records currently stored
   * @return
   */
  public int size();
  public int getBlockSize();
  public int getFreeBlocks();


  public void reserve(int bytes) throws IOException;
  public void setReserve(byte[] bytes) throws IOException;
  public byte[] getReserve() throws IOException;


  public int getFirstRecord();

  public byte[] get(int record) throws IOException;
  public boolean get(int record, byte[] buffer) throws IOException;

  public boolean update(int record, byte[] bytes) throws IOException;
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException;

  public int insert(byte[] bytes) throws IOException;
  public int insert(byte[] bytes, int offset, int length) throws IOException;
  public void insert(int record, byte[] bytes) throws IOException;
  public boolean delete(int record) throws IOException;

  public boolean contains(int record) throws IOException;

  public Iterator<Record> iterator();
  public Iterator<Record> iterator(int record);





}
