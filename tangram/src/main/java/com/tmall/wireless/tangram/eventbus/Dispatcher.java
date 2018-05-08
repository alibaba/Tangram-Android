/*
 * MIT License
 *
 * Copyright (c) 2018 Alibaba Group
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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by longerian on 16/4/27.
 * Dispatch event in main thread
 */
public class Dispatcher extends Handler implements IDispatcher {

    private IDispatcherDelegate mDispatcherDelegate;

    public Dispatcher(IDispatcherDelegate dispatcherDelegate) {
        super(Looper.getMainLooper());
        this.mDispatcherDelegate = dispatcherDelegate;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (mDispatcherDelegate != null) {
            mDispatcherDelegate.dispatch((Event) msg.obj);
        }
        EventPool.sharedInstance().release((Event) msg.obj);
    }

    @Override
    public void start() {

    }

    @Override
    public boolean enqueue(@NonNull Event event) {
        Message msg = obtainMessage();
        msg.obj = event;
        return sendMessage(msg);
    }

    @Override
    public boolean enqueue(@NonNull List<Event> eventList) {
        for (int i = 0, size = eventList.size(); i < size; i++) {
            Message msg = obtainMessage();
            msg.obj = eventList.get(i);
            sendMessage(msg);
        }
        return true;
    }

    @Override
    public void stopSelf() {
        removeCallbacksAndMessages(null);
    }
}
