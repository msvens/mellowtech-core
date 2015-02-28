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

package com.mellowtech.core.bytestorable;

/**
 * Date: 2013-04-17
 * Time: 19:01
 *
 * @author Martin Svensson
 */
public class Record extends  CBRecord <Record.ARecord>{
  
  public class ARecord implements AutoRecord {
    
    @BSField(2) private Integer f1;
    @BSField(1) private String f2;
    
    private Boolean b;
    
    @BSField(3) 
    private void setF3(Boolean b) {this.b = b;}
    private Boolean getF3(){ return b;}
    
    private Short s;
    @BSField(4)
    public void setF4(Short s) {this.s = s; }
    public Short getF4() {return s;}
    
    public String toString(){
      return f1+" "+f2 + getF3() + " " + getF4();
    }
  
  }
  
  public Record(){
    super();
  }

  public Record(Integer field1, String field2, Boolean field3, Short field4){
    this();
    this.record.f1 = field1;
    this.record.f2 = field2;
    this.record.setF3(field3);
    this.record.setF4(field4);
  }

  public Integer getF1(){
    return record.f1;
  }

  public String getF2(){
    return record.f2;
  }
  
  public Boolean getF3(){
    return record.getF3();
  }
  
  public Short getF4(){
    return record.getF4();
  }

  public String toString() {
    return record.toString();
  }


  @Override
  protected ARecord newT() {
    return new ARecord();
  }
}
