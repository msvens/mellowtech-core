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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;


/**
 * Simple counter lock that allows threads to wait for a multi.process
 * to finish. When the counter equals 0 a signalAll() is issued. This
 * means that a call to waitForEmpty() will block until the counter
 * gets down to zero. This can be used to emulate join() to be
 * used in complex situations when join() is not easily employed.
 */
public class ReentrantCounter {
    protected ReentrantLock counterLock = new ReentrantLock();
    protected Condition isEmpty = counterLock.newCondition();
    protected int counter = 0;
    protected int initialValue = 0;
    
    /**
     * Does nothing.
     */
    public ReentrantCounter() {
    } // Default constructor

    /**
     * Sets the counter to an initial value.
     * @param pInitialValue holds the value.
     */
    public ReentrantCounter(int pInitialValue) {
    	  initialValue = pInitialValue;
    	  counter = pInitialValue;
    } // constructor
    
    /**
     * When you start something that needs to be counted for, call
     * increment. Match this with a call to decrement() when you are done.
     * @return the value of the counter.
     */
    public int increment() {
        counterLock.lock();
        int val = ++counter;
        counterLock.unlock();
        return val;
    } // increment
    
    /**
     * When you are done, call this so you decrement the internal counter.
     * When the counter gets to zero, any waiting threads are notified.
     * Note that the counter can never go below 0.
     */
    public void decrement() {
        counterLock.lock();
        if(counter > 0)
        	counter--;
        if(counter == 0)
            isEmpty.signalAll();
        counterLock.unlock();
    } // decrement
    
    /**
     * Just checks the counter.
     * @return the value of the counter.
     */
    public int getCounterValue() {
      counterLock.lock();
      int value = counter;
      counterLock.unlock();
      return value;
    } // getCaounterValue
    
    /**
     * Sets counter to its initial value.
     */
    public void reset() {
    	  counterLock.lock();
    	  counter = initialValue;
    	  counterLock.unlock();
    }  // reset
    
    /**
     * Call this in your thread so you wait until the internal
     * counter is zero.
     */
    public void waitForEmpty() {
        try {
            counterLock.lock();
            while(counter > 0)
                isEmpty.await();
        } catch(InterruptedException ie) {
          CoreLog.L().log(Level.INFO, "", ie);
        } // try-catch
        counterLock.unlock();
    } // waitForEmpty
    
    /**
     * Call this in your thread so you wait until the internal
     * counter is zero or a time out occurs.
     */
    public boolean waitForEmpty(long millis) {
    	  boolean result = false;
      try {
    	long stamp = System.currentTimeMillis();
        counterLock.lock();
        while(System.currentTimeMillis() - stamp < millis && counter > 0)
          result = isEmpty.await(millis, TimeUnit.MILLISECONDS);
      } catch(InterruptedException ie) {
        CoreLog.L().log(Level.INFO, "", ie);
      } // try-catch
      counterLock.unlock();
      return result;
    } // waitForEmpty
} // ReentrantCounter
