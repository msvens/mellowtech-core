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

package org.mellowtech.core.io.compress;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author msvens
 * @since 2018-02-13
 */
public interface CFileWriter extends CFile{


  default long add(byte[] b, int offset, int length) throws IOException{
    ByteBuffer bb = ByteBuffer.wrap(b, offset, length);
    return add(bb);
  }

  default long add(ByteBuffer bb, int position, int length) throws IOException {
    return add(bb.duplicate().position(position).limit(position+length));
  }

  long add(ByteBuffer bb) throws IOException;

}
