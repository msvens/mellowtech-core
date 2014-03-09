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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory for executors where threads are given a name for later inspection.
 *
 * This factory creates all new threads in the same ThreadGroup. If there is a SecurityManager,
 * it uses the group of System.getSecurityManager(), else the group of the
 * thread creating this instance.
 *<p>
 * Each new thread is created as a daemon thread with priority
 * <code>Thread.NORM_PRIORITY</code>.
 *
 * This code was copied from com.digin2.rendezvous.client.RendezvousClient
 *
 * @TODO Perhaps change use in rendezvous client to use this class.
 */
public class NamedThreadFactory implements ThreadFactory
{
    // Thread count, used for thread name suffixes.
    private final AtomicInteger fThreadNumber = new AtomicInteger(1);

    // The base name to give the threads, the thread count will be added.
    private final String fThreadName;

    // The thread group that threads threads should run in.
    private final ThreadGroup fThreadGroup;

    /**
     * Create a new <code>NamedThreadFactory</code>.
     */
    NamedThreadFactory(String pThreadName)
    {
        fThreadName = pThreadName;

        // Get the thread group, this code is based on
        // java.util.concurrent.Executors$DefaultThreadFactory.
        SecurityManager aSecurityManager = System.getSecurityManager();
        if (aSecurityManager != null)
            fThreadGroup = aSecurityManager.getThreadGroup();
        else
            fThreadGroup = Thread.currentThread().getThreadGroup();
    }

    /**
     * Create a new thread.
     *
     * @param pRunnable The runnable to specify for the thread.
     *
     * @return  A new thread.
     */
    public Thread newThread(Runnable pRunnable)
    {
        // Create the thread and set it to run as daemon with normal
        // priority.
        Thread aThread = new Thread(fThreadGroup,
                                    pRunnable,
                                    fThreadName + '-' + fThreadNumber.getAndIncrement(),
                                    0);
        if (!aThread.isDaemon())
            aThread.setDaemon(true);
        if (aThread.getPriority() != Thread.NORM_PRIORITY)
            aThread.setPriority(Thread.NORM_PRIORITY);

        return aThread;
    }
}
