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

package com.tmall.wireless.tangram3.support;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.tmall.wireless.tangram3.support.TimerSupport.OnTickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerTimer implements Runnable, ITimer {
    private Handler mHandler;
    private long mInterval;
    private TimerStatus mStatus;

    private long mStartTS = 0L;

    private Map<OnTickListener, IntervalTickListener> mListeners = new HashMap<>();

    private List<IntervalTickListener> mCopyListeners = new ArrayList<IntervalTickListener>();

    public HandlerTimer(long interval) {
        this(interval, new Handler(Looper.getMainLooper()));
    }

    public HandlerTimer(long interval, Handler handler) {
        if (handler == null) {
            throw new NullPointerException("handler or task must not be null");
        }
        this.mInterval = interval;
        this.mHandler = handler;
        this.mStatus = TimerStatus.Waiting;
    }

    @Override
    public final void run() {
        if (mStatus == TimerStatus.Waiting
                || mStatus == TimerStatus.Paused
                || mStatus == TimerStatus.Stopped) {
            return;
        }

        runTask();

        long delay = mInterval - (System.currentTimeMillis() - mStartTS) % mInterval;
        mHandler.postDelayed(this, delay == 0 ? mInterval : delay);
    }

    /**
     * start timer immediately
     */
    @Override
    public void start() {
        start(false);
    }

    /**
     * @param bySecond true, start timer with interval alignment; false, start timer immediately
     */
    @Override
    public void start(boolean bySecond) {
        if (this.mStatus != TimerStatus.Running) {
            this.mStartTS = bySecond ? 0 : System.currentTimeMillis();
            mHandler.removeCallbacks(this);
            this.mStatus = TimerStatus.Running;

            long delay = mInterval - (System.currentTimeMillis() - mStartTS) % mInterval;
            mHandler.postDelayed(this, delay);
        }
    }

    /**
     * pause timer
     */
    @Override
    public void pause() {
        this.mStatus = TimerStatus.Paused;
        mHandler.removeCallbacks(this);
    }

    /**
     * resume timer
     */
    @Override
    public void restart() {
        mHandler.removeCallbacks(this);
        this.mStatus = TimerStatus.Running;
        mHandler.postDelayed(this, mInterval);
    }

    /**
     * stop timer
     */
    @Override
    public void stop() {
        mStatus = TimerStatus.Stopped;
        mHandler.removeCallbacks(this);
    }

    @Override
    public void clear() {
        mListeners.clear();
    }

    /**
     * cancel timer
     */
    @Override
    public void cancel() {
        mStatus = TimerStatus.Stopped;
        mHandler.removeCallbacks(this);
    }

    /**
     * @return current status
     */
    @Override
    public TimerStatus getStatus() {
        return mStatus;
    }

    @Override
    public void register(int interval, OnTickListener onTickListener, boolean intermediate) {
        mListeners.put(onTickListener, new IntervalTickListener(interval, onTickListener, intermediate));
        start(false);
    }

    @Override
    public void unregister(OnTickListener onTickListener) {
        mListeners.remove(onTickListener);
    }

    @Override
    public boolean isRegistered(OnTickListener onTickListener) {
        return mListeners.containsKey(onTickListener);
    }

    public void runTask() {
        mCopyListeners.clear();
        mCopyListeners.addAll(mListeners.values());
        for (int i = 0, size = mCopyListeners.size(); i < size; i++) {
            IntervalTickListener listener = mCopyListeners.get(i);
            listener.onTick();
        }
        if (mListeners.isEmpty()) {
            stop();
        }
    }

    /**
     * Timer status
     */
    @Keep
    public enum TimerStatus {

        /**
         * Waiting
         */
        Waiting(0, "Waiting"),
        /**
         * Running
         */
        Running(1, "Running"),
        /**
         * Paused
         */
        Paused(-1, "Paused"),
        /**
         * Stopped
         */
        Stopped(-2, "Stopped");

        private int code;
        private String desc;

        TimerStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
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
