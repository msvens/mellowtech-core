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

package org.mellowtech.core.io.impl;

import org.mellowtech.core.io.Record;
import org.mellowtech.core.io.RecordFile;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.Iterator;
import java.util.Map;

/**
 * @author msvens
 * @since 23/04/16
 */
public class MappedBlockFile implements RecordFile {



  @Override
  public void clear() throws IOException {

  }

  @Override
  public void close() throws IOException {

  }

  @Override
  public Map<Integer, Integer> compact() throws IOException {
    return null;
  }

  @Override
  public boolean contains(int record) throws IOException {
    return false;
  }

  @Override
  public boolean delete(int record) throws IOException {
    return false;
  }

  @Override
  public MappedByteBuffer getMapped(int record) throws UnsupportedOperationException {
    return null;
  }

  @Override
  public boolean get(int record, byte[] buffer) throws IOException {
    return false;
  }

  @Override
  public int getBlockSize() {
    return 0;
  }

  @Override
  public int getFirstRecord() {
    return 0;
  }

  @Override
  public int getFreeBlocks() {
    return 0;
  }

  @Override
  public byte[] getReserve() throws IOException, UnsupportedOperationException {
    return new byte[0];
  }

  @Override
  public int insert(byte[] bytes, int offset, int length) throws IOException {
    return 0;
  }

  @Override
  public void insert(int record, byte[] bytes, int offset, int length) throws IOException {

  }

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public Iterator<Record> iterator() throws UnsupportedOperationException {
    return null;
  }

  @Override
  public Iterator<Record> iterator(int record) throws UnsupportedOperationException {
    return null;
  }

  @Override
  public MappedByteBuffer mapReserve() throws IOException, UnsupportedOperationException {
    return null;
  }

  @Override
  public void remove() throws IOException {

  }

  @Override
  public boolean save() throws IOException {
    return false;
  }

  @Override
  public void setReserve(byte[] bytes) throws IOException, UnsupportedOperationException {

  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public long fileSize() throws IOException {
    return 0;
  }

  @Override
  public boolean update(int record, byte[] bytes, int offset, int length) throws IOException {
    return false;
  }
}
