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
import android.support.v4.util.Pools;

/**
 * Created by longerian on 16/4/26.
 *
 * Event pool to recycle consumed event.
 */
class EventPool {

    private Pools.SynchronizedPool<Event> recyclePool = new Pools.SynchronizedPool<Event>(25);

    private static class EventPoolHolder {
        private static final EventPool sharedInstance = new EventPool();
    }

    private EventPool() {
    }

    public static EventPool sharedInstance()  {
        return EventPoolHolder.sharedInstance;
    }

    @NonNull
    public Event acquire() {
        Event instance = recyclePool.acquire();
        if (instance == null) {
            instance = new Event();
        }
        return instance;
    }

    public boolean release(@NonNull Event event) {
        event.type = null;
        event.sourceId = null;
        if (event.args != null) {
            event.args.clear();
        }
        event.eventContext = null;
        return recyclePool.release(event);
    }

}
