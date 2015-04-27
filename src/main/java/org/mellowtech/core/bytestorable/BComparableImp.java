/**
 * 
 */
package org.mellowtech.core.bytestorable;

/**
 * @author msvens
 *
 */
public abstract class BComparableImp <A,B extends BComparable<A,B>> implements BComparable <A,B>{
  
  protected A value;
  
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
