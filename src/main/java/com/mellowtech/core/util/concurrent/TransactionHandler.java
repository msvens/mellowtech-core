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

import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * This code belongs to digin2 Concurrent - a simplification of J2EE.
 *
 * This is a rather complex module that handles multiple transactions. The
 * following comments outline the idea and not the actual code below. The code
 * only uses one entry method <code>execute</code> which is synchronized to
 * emulate a pipe and a flag pIsSequential that if true will ensure that the
 * sequential transaction is the only transaction being executed.
 *
 * Example: The code is done so that an index switch/commit event should be
 * called with execute(switchRunnable, true) to ensure the integrity of the
 * system.
 *
 * There are two main types of transactions: lookup and modifier events. Lookups
 * are typically queries and requests for documents etc. Modifiers are
 * operations that change the contents of the index. A typical modifier event is
 * an indexing signal or a document append.
 *
 * All events are posted through a common pipe channel. The pipe consumer
 * decides on the overall logic.
 *
 * All lookup events are handled by a thread queue, possibly parallel. All
 * modifier events are handled sequentially in the order they arrive. When a
 * modifier event is committed the index typically needs reloading of sort. At
 * that time the existing lookup events are allowed to run through and then the
 * commit event takes place. Any pending lookup has to wait until the commit is
 * done. Therefore a pipe strategy is used as a 1st line of defence. When a
 * commit is posted, no more events are read through the main transaction pipe
 * and existing lookups are allowed to terminate. When the lookups are done, the
 * commit takes place and then the pipe is processed as usual.
 *
 * Below is a schematic picture of the transaction flow for anticipated use in
 * index search engine.
 *
 * --> Index type --> sequential, commit will block queries / Transaction =>
 * Pipe => Transaction type \ --> Search type --> parallel, processed by thread
 * queue
 */
public class TransactionHandler {
  // default size for max number of elements in queue.
  public final static int kQueueSize = 100;

  // Queue for incoming requests, fifo and bounded.
  BlockingQueue<Runnable> fQueue = null;

  // The actual transaction handler, the executable thread-pool.
  InternalTransactionHandler fTransactionExecutor;

  /**
   * Default constructor, initializes the transaction handler to
   * <ul>
   * <li> minimum 2 threads. </li>
   * <li> maximum 5 threads. </li>
   * <li> 500 seconds time-out. </li>
   * <li> maximum 100 elements in queue. </li>
   * </ul>
   */
  public TransactionHandler() {
    fQueue = new ArrayBlockingQueue<Runnable>(kQueueSize, true);
    fTransactionExecutor = new InternalTransactionHandler(2, 5, 500,
        TimeUnit.SECONDS, fQueue);
  } // default constructor

  /**
   * Constructor setting the max (and min) number of threads to be used.
   *
   * Sets the values to:
   * <ul>
   * <li> minimum 'pMaxNumThreads'/2 threads. </li>
   * <li> maximum 'pMaxNumThreads' threads. </li>
   * <li> 500 seconds time-out. </li>
   * <li> maximum 'pMaxQueueSize' elements in queue. </li>
   * </ul>
   *
   * @param pMaxNumThreads max number of threads
   * @param pMaxQueueSize max queue size
   */
  public TransactionHandler(int pMaxNumThreads, int pMaxQueueSize) {
    fQueue = new ArrayBlockingQueue<Runnable>(pMaxQueueSize, true);
    int minNumThreads = pMaxNumThreads / 2;
    if (minNumThreads <= 0)
      minNumThreads = 1;
    fTransactionExecutor = new InternalTransactionHandler(minNumThreads,
        pMaxNumThreads, 500, TimeUnit.SECONDS, fQueue);
  } // constructor

  /**
   * Constructor setting the max (and min) number of threads to be used,
   * the max queue size and the timeout in seconds.
   *
   * Sets the values to:
   * <ul>
   * <li> minimum 'pMaxNumThreads'/2 threads. </li>
   * <li> maximum 'pMaxNumThreads' threads. </li>
   * <li> 'pTimeOutSeconds' seconds time-out. </li>
   * <li> maximum 'pMaxQueueSize' elements in queue. </li>
   * </ul>
   *
   * @param pMaxNumThreads max number of threads
   * @param pMaxQueueSize max queue size
   * @param pTimeOutSeconds timeout in seconds
   * @param pStartSuspended if true the executor starts in suspended mode and must be resumed before any
   * work will be done.
   */
  public TransactionHandler(int pMaxNumThreads, int pMaxQueueSize, int pTimeOutSeconds,
		  boolean pStartSuspended) {
    fQueue = new ArrayBlockingQueue<Runnable>(pMaxQueueSize, true);
    int minNumThreads = pMaxNumThreads / 2;
    if (minNumThreads <= 0)
      minNumThreads = 1;
    fTransactionExecutor = new InternalTransactionHandler(minNumThreads,
        pMaxNumThreads, pTimeOutSeconds, TimeUnit.SECONDS, fQueue);
    if(pStartSuspended)
    	pause();
  } // constructor

	/**
	 * Closes transaction executor by invoking shut-down.
	 */
	public void close() {
		if(fTransactionExecutor != null)
			fTransactionExecutor.shutdown();
	} // close

  /**
   * Method to get a Runnable executed. The method is synchronized to emulate a
   * pipe so in case of a SEQUENTIAL task the existing tasks are ensured to run
   * through before the SEQUENTIAL task runs which in turn is before any other
   * tasks can run. Note that this method returns after the Runnable is
   * submitted for execution. If the calling thread requires that it waits for
   * the Runnable to finish use execute instead. The execute wraps the Runnable
   * in a Transaction and does not return until the Runnable is done..
   *
   * @param pRunnable
   *          holds the stuff to run.
   * @param pIsSequential
   *          is true if no other events are llowed to be processed before this
   *          pRunnable is done. Currently active tasks are allowed to finish,
   *          then pRunnable is executed, and then the executor starts over
   *          again.
   * @return true if task was added to queue, if false is returned then the task
   *         was NOT inserted into the task queue (probably the queue got full
   *         and the best is to wait for a while).
   */
  public synchronized boolean _execute(Runnable pRunnable, boolean pIsSequential) {
    // We do have a task that cannot allow any processing until it is completed.
    if (pIsSequential) {
      fTransactionExecutor.pauseIncoming();
      fTransactionExecutor.fCounterLock.lock();

      boolean wasInterrupted = false;
      while (fTransactionExecutor.fActiveTaskCounter != 0)
        try {
          fTransactionExecutor.fIsEmpty.await();
        } catch (InterruptedException e) {
          wasInterrupted = true;
        }
      if (wasInterrupted == false)
        pRunnable.run();
      fTransactionExecutor.fCounterLock.unlock();
      fTransactionExecutor.resumeIncoming();

      return !wasInterrupted;
    } // if sequential

    // This is a normal Runnable, just thread it in the pool.
    try {
      fTransactionExecutor.execute(pRunnable);
    } catch (RejectedExecutionException ree) {
      return false;
    }
    return true;
  } // _execute

  /**
   * Public non-synchronized execute version. Wraps Runnables and waits for its
   * completion. If this method is called from several threads each will be used
   * async, but it is thread safe of course since this is the exact use of this
   * method.
   *
   * @param pRunnable
   *          holds Runnable to execute.
   * @param pIsSequential
   *          true if it should run alone.
   * @param pMillis
   *          holds the number of millisecods before timeout is reached.
   * @return true if Runnable was submitted, false if rejected - probably pool
   *         overflow in that case.
   */
  public boolean execute(Runnable pRunnable, boolean pIsSequential, long pMillis) {
    // Make wrapper for Runnable - we can have a pool of these if necessary.
    Transaction trans = new Transaction();
    trans.reset(pRunnable);
    boolean isSubmitted = false;

    // Submit trans for execution.
    isSubmitted = _execute(trans, pIsSequential);
    if (!isSubmitted)
      return false;

    // Let calling thread wait for completion.
    return trans.waitFor(pMillis);
  } // execute

  /**
   * Public thread safe version of execute that takes a Transaction. benefit is
   * that caller can use the transaction and wait in a caller specific manner
   * for completion.
   *
   * @param pTransaction
   *          holds the runnable.
   * @param pIsSequential
   *          to be true if transaction should run alone when no other
   *          transactions are eecuted. Blocks any other in the pipe until
   *          completion.
   */
  public boolean execute(Transaction pTransaction, boolean pIsSequential) {
    boolean isSubmitted = false;

    // Submit trans for execution.
    isSubmitted = _execute(pTransaction, pIsSequential);
    if (!isSubmitted)
      return false;

    return true;
  } // execute

  /**
   * Pauses execution of incoming tasks. Note that currently executing tasks are
   * awaited to.
   */
  public void pause() {
    fTransactionExecutor.pauseIncoming();
  } // pause

  /**
   * Attempts to remove a submitted Runnable from the task list. If the Runnable has started its
   * execution it will not be removed. So in effect this affects the queue only - that is, the
   * Runnable is removed from the queue if it still resides there.
   *
   * @param pTask holds the instance to be removed.
   */
  public void remove(Runnable pTask) {
	 fTransactionExecutor.remove(pTask);
  } // remove

  /**
   * Resumes execution of incoming transactions.
   */
  public void resume() {
    fTransactionExecutor.resumeIncoming();
  } // resume

  public List<Runnable> shutDownNow() {
    return this.fTransactionExecutor.shutdownNow();
  }

  /**
   * A pausable executor that can be awaited for until all presently running
   * Runnables have finished.
   */
  static private class InternalTransactionHandler extends ThreadPoolExecutor {

      // Base name for thread factories, an instance count will be appended.
      static private final String kThreadFactoryBaseName = "InternalTransactionHandler-";

      // Counter for the number of created InternalTransactionHandler instances,
      // used for NamedThreadFactory name suffixes.
      static private final AtomicInteger cInstanceCounter = new AtomicInteger(1);

    // Active task counter; this holds the number of Runnables being executed.
    public ReentrantLock fCounterLock = new ReentrantLock();
    public Condition fIsEmpty = fCounterLock.newCondition();
    public int fActiveTaskCounter = 0;

    // Pause - management
    private boolean fIsPaused;
    private ReentrantLock fPauseLock = new ReentrantLock();
    private Condition unpaused = fPauseLock.newCondition();

    public InternalTransactionHandler(int corePoolSize, int maximumPoolSize,
        long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
    {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
            new NamedThreadFactory(kThreadFactoryBaseName + cInstanceCounter.getAndIncrement()));
    } // constructor

    protected void afterExecute(Runnable r, Throwable t) {
      super.afterExecute(r, t);

      // Decrease active runnable counter and signal to the fIsEmpty condition.
      fCounterLock.lock();
      fActiveTaskCounter--;
      if (fActiveTaskCounter == 0)
        fIsEmpty.signalAll();
      fCounterLock.unlock();
    } // afterExecute

    protected void beforeExecute(Thread t, Runnable r) {
      super.beforeExecute(t, r);

      // If transactions are paused, wait until resumeIncoming() gets called.
      fPauseLock.lock();
      try {
        while (fIsPaused)
          unpaused.await();
      } catch (InterruptedException ie) {
        t.interrupt();
      } finally {
        fPauseLock.unlock();
      }

      // Increase task counter.
      fCounterLock.lock();
      fActiveTaskCounter++;
      fCounterLock.unlock();
    } // beforeExecute

    public void pauseIncoming() {
      fPauseLock.lock();
      try {
        fIsPaused = true;
      } finally {
        fPauseLock.unlock();
      }
    } // pauseIncoming

    public void resumeIncoming() {
      fPauseLock.lock();
      try {
        fIsPaused = false;
        unpaused.signalAll();
      } finally {
        fPauseLock.unlock();
      }
    } // resumeIncoming
  } // InternalTransactionHandler

  /**
   * @param args
   */
  int counter = 0;

  public Runnable getTestRun(final int pNumSeconds, final String pStr) {
    final int internal = ++counter;

    Runnable r = new Runnable() {
      int fNumSeconds = pNumSeconds;
      int fId = internal;
      long start = 0, end = 0;
      String str = pStr;
      Object obj = new Object();

      public void run() {
        start = System.currentTimeMillis();
        end = start + fNumSeconds * 1000;
        boolean caughtException = false;
        // sleep a while before testing the time. This relieves CPU alot!
        while (System.currentTimeMillis() < end) {
          try {
            synchronized (obj) {
              obj.wait(200);
            }
          } catch (InterruptedException ignore) {
            CoreLog.L().log(Level.FINE, "", ignore);
            caughtException = true;
          }
        }
      }
    }; // The test runnable

    return r;
  } // getTestRun

  public static void main(String[] args) {
    /**
     * OBSERVER! This example uses the _execute instead of execute to allow
     * parallel execution in the same calling thread - or so it appears anyway.
     * If multi threads are to submit a Runnable but each should wait for their
     * respective Runnable to end, then use the execute method instead. (But
     * then you have to make several threads in order to observe correct
     * behaviour of course, since each call awaits completion).
     */
    /*
     * Runnable r = h.getTestRun(10, "First"); h._execute(r, false);
     *
     * r = h.getTestRun(8, "Second"); h._execute(r, false);
     *
     * r = h.getTestRun(3, "Third - Sequential"); h._execute(r, true);
     *
     * r = h.getTestRun(7, "Fourth"); h._execute(r, false);
     *
     */

    final TransactionHandler transactionHandler = new TransactionHandler();
    final Random random = new Random();
    for (int i = 0; i < 100; i++) {
      final boolean isSequential = random.nextInt(100) > 95 ? true : false;
      final int millis = random.nextInt(1000) + 1000;
      final int index = i;
      Thread creator = new Thread(new Runnable() {
        public void run() {
          Runnable toRun
            = transactionHandler.getTestRun(millis/1000, "\t[" + index + "," + (isSequential ? "isSequential": "") + "]");
          Transaction transaction = new Transaction(toRun);
          transactionHandler.execute(transaction, isSequential);
      }
      });
      creator.start();
    }

    Thread waiter = new Thread(new Runnable() {
      public void run() {
        try{Thread.sleep(5000);
        }
        catch(InterruptedException ie) {
        }

        List<Runnable> old = transactionHandler.shutDownNow();
      }
    });
    waiter.start();
  } // main

} // TransactionHandler

