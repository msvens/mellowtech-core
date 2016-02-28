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

package org.mellowtech.core.bytestorable;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


/**
 * BStorable wrapper for String that compresses the data using LZ4. This
 * is useful when serializing very large Strings. The string is compressed
 * at initialization so every call to get will uncompress the data
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @version 3.0.4
 * @see CBString
 */
public class CBZString implements BStorable<String, CBZString> {

  private static LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();
  private static LZ4FastDecompressor decompressor = LZ4Factory.fastestInstance().fastDecompressor();

  protected byte[] comp = null;
  protected int compressLength = 0;
  protected int origLength;

  /**
   * Initialize this CBZString with null. Useful when using this as a
   * template instance
   */
  public CBZString() {
    this((String) null);
  }

  /**
   * Initialize this CBZString with char array
   * @param str array to compress
   */
  public CBZString(char[] str) {
    if (str == null || str.length == 0) {
      return;
    }
    setData(UtfUtil.encode(str));


  }

  /**
   * Initialize this CBZString with string
   * @param str string to compress
   */
  public CBZString(String str) {

    if (str == null || str.isEmpty()) {
      return;
    }
    setData(str.getBytes(StandardCharsets.UTF_8));

  }

  private CBZString(byte[] compressed, int origLength) {
    this.comp = compressed;
    this.origLength = origLength;
    this.compressLength = compressed.length;
  }

  //size indicator + uncompressed length + compressedBytes.length
  @Override
  public int byteSize() {
    return CBUtil.byteSize(4 + compressLength, true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, true);
  }

  @Override
  public CBZString from(ByteBuffer bb) {
    int nBytes = CBUtil.getSize(bb, true);
    //first read orig length
    int ol = bb.getInt();
    byte[] ctmp = new byte[nBytes - 4];
    bb.get(ctmp);
    return new CBZString(ctmp, ol);
  }

  @Override
  public String get() {

    if (comp == null) return null;
    byte[] dest = new byte[origLength];
    decompressor.decompress(comp, dest);
    try {
      return UtfUtil.decode(dest);
    } catch(Exception e){
      throw new Error(e);
    }
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(4 + compressLength, bb, true);
    bb.putInt(origLength);
    bb.put(comp, 0, compressLength);
  }

  private void setData(byte[] data) {
    origLength = data.length;
    comp = new byte[compressor.maxCompressedLength(origLength)];
    compressLength = compressor.compress(data, comp);
  }
}
