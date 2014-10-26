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

package com.mellowtech.core.examples;

import com.mellowtech.core.bytestorable.*;

import java.nio.ByteBuffer;

/**
 * Date: 2013-04-14
 * Time: 12:08
 *
 * @author Martin Svensson
 */
//START SNIPPET: c2-class
public class Container2 extends CBAuto <Container2> {

  @BSField(2) private Integer f1;
  @BSField(1) private String f2;

  public Container2(){
    super();
  }

  public Container2(Integer field1, String field2){
    this();
    this.f1 = field1;
    this.f2 = field2;
  }
}
//END SNIPPET: c2-class