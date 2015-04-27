/**
 * 
 */
package org.mellowtech.core.bytestorable;

/**
 * @author msvens
 *
 */
public abstract class BStorableImp <A,B extends BStorable<A,B>> implements BStorable <A,B>{
  
  protected A value;
  
  public BStorableImp(A value){
    this.value = value;
  }
  
  @Override
  public A get(){return value;}
  
  @Override
  public int hashCode(){return value.hashCode();}
  
  @Override
  public String toString(){return value.toString();}
  
  

}
