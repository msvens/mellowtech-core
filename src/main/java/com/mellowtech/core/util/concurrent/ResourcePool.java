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

package com.mellowtech.core.util.concurrent;

import com.mellowtech.core.CoreLog;

import java.util.Iterator;
import java.util.Stack;


/**
* This code belongs to Asimus Concurrent - a simplification of J2EE.
* 
* Implements a fixed size resource handler. Users request a resource
* and if no resource is free, the user waits for one to become free.
* This class is extremely simple but enough for most advanced purposes.
* 
*/
public class ResourcePool<E> {
   /** Stack of resource objects. */
   Stack<E>	fPool;
   
   /** initial size of pool, the size when all resources are in the stack (bot used that is). */
   int   fMaxSize;
   
   /** Empty resource pool. Objects must be added via put(obj) before use. */
   public ResourcePool() {
     fPool = new Stack<E>();
     fMaxSize = 0;
   } // Empty constructor
   
   /** Inits the resource pool with a set of objects. */
   public ResourcePool(E[] pResourceObjects) {
     this();
     for(int i = 0; i < pResourceObjects.length; i++)
       fPool.push(pResourceObjects[i]);
     fMaxSize = pResourceObjects.length;
   } // Constructor
   
   /** Fetches an iterator for the elements in the pool. */
   public Iterator<E> getIterator() {
	   return fPool.iterator();
   } // getIterator
   
   /** Returns an available object or waits until one gets free to use. */
   public E get() {
	   synchronized(fPool) {
	       while(fPool.empty()) 
		        try {
		          fPool.wait();
		        }
		        catch (InterruptedException e) {
              CoreLog.L().finest("Resources: InterruptedException caught.");
		          return null;
		        }
		   E obj = fPool.pop();
		   return obj;
		 } // synchronized
   } // get
   
   /** Returns an available object or waits until one gets free to use. 
    * has a time out. throws exception if time-out is reached. */
   public E get(long timeOut) throws InterruptedException {
	   synchronized(fPool) {
	       while(fPool.empty()) 
		        try {
		          fPool.wait(timeOut);
		        }
		        catch (InterruptedException e) {
		          throw new InterruptedException(e.getMessage());
		        }
		   E obj = fPool.pop();
		   return obj;
		 } // synchronized
   } // get
   
      
   
   /** Adds (returns) an object to the pool. */
   public void put(E pObject) {
       synchronized(fPool) {
	        fPool.push(pObject);
	        fPool.notifyAll();
	     }
   } // put
   
   /** Returns the current size of the pool, how many objects that are in the pool. */
   public int size() {
	   return fPool.size();
   }
} // ResourcePool
