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

package org.mellowtech.core.bytestorable;

import org.mellowtech.core.bytestorable.BSField;
import org.mellowtech.core.bytestorable.CBAuto;

/**
 * Date: 2013-04-17
 * Time: 19:01
 *
 * @author Martin Svensson
 */
public class AutoContainer extends  CBAuto <AutoContainer>{

  @BSField(2)
  private Integer f1;

  @BSField(1)
  private String f2;

  public AutoContainer(){
    super();
  }

  public AutoContainer(Integer field1, String field2){
    this.f1 = field1;
    this.f2 = field2;
  }

  public Integer getF1(){
    return f1;
  }

  public String getF2(){
    return f2;
  }

  public String toString(){
    return f1+" "+f2;
  }
}
