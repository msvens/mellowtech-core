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

/**
 * @author msvens
 * @since 2017-01-28
 */
public class RecordCodecTest extends CodecTemplate<TestRecord> {
  @Override
  public TestRecord val(int idx) {
    return idx == 0 ? new TestRecord(0) : new TestRecord(1);
  }

  //6 bytes (for field) + 2 bytes for index + 2 bytes for number of elements + 1 byte for size
  @Override
  public int size(int idx) {
    return 11;
  }

  @Override
  public BCodec<TestRecord> codec() {
    return new RecordCodec<>(TestRecord.class);
  }

}
