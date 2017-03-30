/*
 * MIT License
 *
 * Copyright (c) 2017 Alibaba Group
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tmall.wireless.tangram.eventbus;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by longerian on 16/4/26.
 *
 * Not used currently
 */
class AsyncDispatcher extends Thread implements IDispatcher {

    private final int STATE_INIT = 0;
    private final int STATE_STARTED = 1;
    private final int STATE_IDLE = 2;
    private final int STATE_DISPATCHING = 3;
    private final int STATE_SHUTDOWN = 4;

    private final Object LOCK = new Object();

    private ConcurrentLinkedQueue<Event> queue;

    private volatile int status;

    private volatile boolean isRunning;

    private IDispatcherDelegate mDispatcherDelegate;

    public AsyncDispatcher(IDispatcherDelegate dispatcherDelegate) {
        super("Tangram-Dispatcher");
        this.mDispatcherDelegate = dispatcherDelegate;
        status = STATE_INIT;
        queue = new ConcurrentLinkedQueue<Event>();
    }

    @Override
    public synchronized void start() {
        super.start();
        isRunning = true;
    }

    @Override
    public void stopSelf() {
        isRunning = false;
        status = STATE_SHUTDOWN;
        interrupt();
        while (!queue.isEmpty()) {
            Event event = queue.poll();
            EventPool.sharedInstance().release(event);
        }
        queue.clear();
    }

    public int getStatus() {
        return status;
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public boolean enqueue(@NonNull List<Event> eventList) {
        if (status == STATE_SHUTDOWN) {
            for (int i = 0, size = eventList.size(); i < size; i++) {
                EventPool.sharedInstance().release(eventList.get(i));
            }
            eventList.clear();
            return false;
        }
        boolean result = queue.addAll(eventList);
        synchronized (LOCK) {
            LOCK.notify();
        }
        return result;
    }

    @Override
    public boolean enqueue(@NonNull Event event) {
        if (status == STATE_SHUTDOWN) {
            EventPool.sharedInstance().release(event);
            return false;
        }
        boolean result = queue.offer(event);
        synchronized (LOCK) {
            LOCK.notify();
        }
        return result;
    }

    @Override
    public void run() {
        super.run();
        status = STATE_STARTED;
        while (isRunning) {
            if (queue.isEmpty()) {
                status = STATE_IDLE;
                synchronized (LOCK) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            Event event = queue.poll();
            if (event != null) {
                status = STATE_DISPATCHING;
                if (mDispatcherDelegate != null) {
                    mDispatcherDelegate.dispatch(event) ;
                }
                EventPool.sharedInstance().release(event);
            }
        }
    }

}
