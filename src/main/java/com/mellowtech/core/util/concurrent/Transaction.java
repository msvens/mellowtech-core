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

/** Allows a mechanism through Condition to wait for completion in a calling thread. 
   * This is important so that a caller does not return until the given condition is
   * satisfied. This type of transaction is used internally in TransactionHandler to wrap
   * Runnables. A typical scenario would be something like: <br>
   * 
   * Query query = new Query( "Uken7"...); <br>
   * XirTransaction trans = getFreeTransaction(); <br>
   * execute( trans.reset(query) ); <br>
   * trans.waitFor(); <br>
   * QueryResult result = query.getResult(); <br>
   * 
   * The above creates a query, inserts it into a XirTransaction and waits for
   * the transaction to return after being completed.
   * */
	public class Transaction implements Runnable {
		ReentrantCounter counter = new ReentrantCounter();
    Runnable      fDoer;
    Exception fException = null;
    
    public Transaction() {
      fDoer = null;
    } // default constructor
    
    public Transaction(Runnable pDoer) {
    	  reset(pDoer);
    } // Minimum useful constructor
    
    public Transaction clear() {
    	  counter.counter =0;
    	  fDoer = null;
    	  fException = null;
    	  return this;
    } // clear
    
    public Exception getException() {
    	  return fException;
    }
    
    public Transaction reset(Runnable pDoer) {
    	  counter.counter = 1;
    	  fDoer = pDoer;
    	  fException = null;
    	  return this;
    } // reset
        
    public void run() {
    	  try{
        fDoer.run();
    	  } catch(Exception e) {
    	  	  counter.decrement();
    	  	  fException = e;
    	  	  return;
    	  }
    	  counter.decrement();
    } // run
    
    public void waitFor() {
      counter.waitForEmpty();	
    } // waitFor
    
    public boolean waitFor(long pMillis) {
      return counter.waitForEmpty(pMillis);	
    } // waitFor
    
	} // Class Transaction