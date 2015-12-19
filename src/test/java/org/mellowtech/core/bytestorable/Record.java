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


/**
 * Date: 2013-04-17
 * Time: 19:01
 *
 * @author Martin Svensson
 */
public class Record extends  CBRecord <Record.ARecord, Record>{
  
  public class ARecord implements AutoRecord {    
    @BSField(1) public Integer f1;

    @Override
    public boolean equals(Object obj) {
      if(!(obj instanceof ARecord)) return false;
      ARecord o = (ARecord) obj;
      if(o.f1 == null && f1 == null) return true;
      if(o.f1 == null || f1 == null) return false;
      return o.f1.equals(f1);
    }
  }
  
  public Record(){
    super();
  }

  public Record(Record.ARecord record){
    super(record);
  }

  @Override
  protected ARecord newA() {
    return new ARecord();
  }
}