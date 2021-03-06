
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
 * Subclasses of BRecord should let their "record" implement
 * this interface. and annotate whatever fields that should
 * be included int the serialization of the record.
 *
 * Annotated fields has to be of a type that can be encoded using
 * ObjectCodec
 *
 * <p>
 * To create a record with two fields you would simply implement
 * BRecord and add your annotated fields
 * </p>
 * <pre style="code">
 * public class ARecord implements BRecord {
 *   ...
 *   {@literal @}BField(1) public Integer f1;
 *   {@literal @}BField(2) public String f2;
 *   ...
 * }
 * </pre>
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 * @see RecordCodec
 * @see ObjectCodec
 */

public interface BRecord {

}
