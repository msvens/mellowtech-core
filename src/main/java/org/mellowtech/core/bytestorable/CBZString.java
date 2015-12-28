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
 * Created by msvens on 26/12/15.
 */
public class CBZString implements BStorable<String, CBZString> {

  private static LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();
  private static LZ4FastDecompressor decompressor = LZ4Factory.fastestInstance().fastDecompressor();

  protected byte[] comp = null;
  protected int compressLength = 0;
  protected int origLength;

  public CBZString() {
    this((String)null);
  }

  public CBZString(char[] str){
    if(str == null || str.length == 0){
      return;
    }
    setData(UtfUtil.toBytes(str));


  }

  public CBZString(String str) {

    if (str == null || str.isEmpty()) {
      return;
    }
    setData(str.getBytes(StandardCharsets.UTF_8));

  }

  private void setData(byte[] data){
    origLength = data.length;
    comp = new byte[compressor.maxCompressedLength(origLength)];
    compressLength = compressor.compress(data, comp);
  }

  private CBZString(byte[] compressed, int origLength) {
    this.comp = compressed;
    this.origLength = origLength;
    this.compressLength = compressed.length;
  }

  @Override
  public String get() {

    if (comp == null) return null;
    byte[] dest = new byte[origLength];
    decompressor.decompress(comp, dest);
    return new String(dest, StandardCharsets.UTF_8);

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
  public void to(ByteBuffer bb) {
    CBUtil.putSize(4 + compressLength, bb, true);
    bb.putInt(origLength);
    bb.put(comp, 0, compressLength);
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
}
