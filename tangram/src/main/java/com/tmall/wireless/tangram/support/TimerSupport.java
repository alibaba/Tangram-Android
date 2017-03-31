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

package com.tmall.wireless.tangram.support;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by villadora on 15/9/8.
 */
public class TimerSupport implements Runnable {

    private static final int MILLISECOND = 1000;

    private Map<OnTickListener, IntervalTickListener> mListeners = new HashMap<>();

    private List<IntervalTickListener> mCopyListeners = new ArrayList<IntervalTickListener>();

    private HandlerTimer mDefaultTimer = new HandlerTimer(MILLISECOND, this);

    public void register(int interval, @NonNull OnTickListener onTickListener) {
        register(interval, onTickListener, false);
    }

    /**
     * onTickListener will store as weak reference, don't use anonymous class here
     *
     * @param interval       how many seconds of interval that the listener will be called in
     * @param onTickListener listener
     * @param intermediate   whether execute onTick intermediately
     */
    public void register(int interval, @NonNull OnTickListener onTickListener, boolean intermediate) {
        mListeners.put(onTickListener, new IntervalTickListener(interval, onTickListener, intermediate));
        mDefaultTimer.start(false);
    }

    /**
     * unregister a onTickListener
     * @param onTickListener
     */
    public void unregister(@NonNull OnTickListener onTickListener) {
        mListeners.remove(onTickListener);
    }

    /**
     * @return current timer status
     */
    public HandlerTimer.TimerStatus getStatus() {
        return mDefaultTimer.getStatus();
    }

    /**
     * restart timer
     */
    public void restart() {
        mDefaultTimer.restart();
    }

    /**
     * pause timer
     */
    public void pause() {
        mDefaultTimer.pause();
    }

    /**
     * clear timer listener and stop timer
     */
    public void clear() {
        mListeners.clear();
        mDefaultTimer.stop();
    }

    public boolean isRegistered(@NonNull OnTickListener onTickListener) {
        return mListeners.containsKey(onTickListener);
    }

    public void run() {
        mCopyListeners.clear();
        mCopyListeners.addAll(mListeners.values());
        for (int i = 0, size = mCopyListeners.size(); i < size; i++) {
            IntervalTickListener listener = mCopyListeners.get(i);
            listener.onTick();
        }
        if (mListeners.isEmpty()) {
            mDefaultTimer.stop();
        }
    }


    /**
     * tick lister, called at every registered interval time passed
     */
    public interface OnTickListener {
        void onTick();
    }

    static final class IntervalTickListener {
        private int interval;
        private int count;

        private OnTickListener mListener;

        IntervalTickListener(int interval, @NonNull OnTickListener onTickListener, boolean intermediate) {
            this.interval = interval;
            this.count = 0;
            this.mListener = onTickListener;

            if (intermediate) {
                onTickListener.onTick();
            }
        }

        void onTick() {
            count = (count + 1) % interval;
            if (count == 0) {
                if (mListener != null) {
                    mListener.onTick();
                }
            }
        }

    }
}
