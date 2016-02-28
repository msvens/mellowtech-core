
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

/**
 * Template for implementing BComparables. Adds a holder for the value and
 * and a default constructor
 *
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 3.0.1
 * @param <A> type of value
 * @param <B> self type
 */
public abstract class BComparableImp <A,B extends BComparable<A,B>> implements BComparable <A,B>{

  /**
   * Wrapped value
   */
  protected A value;

  /**
   * Instantiates this BComparableImp with a value
   * @param value value to set
   */
  public BComparableImp(A value){
    this.value = value;
  }
  
  @Override
  public A get(){return value;}
  
  @Override
  public int hashCode(){return value.hashCode();}
  
  @Override
  public String toString(){return value.toString();}
  
  

}
