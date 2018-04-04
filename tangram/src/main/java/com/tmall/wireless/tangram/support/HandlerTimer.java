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

package com.tmall.wireless.tangram.support;

import android.os.Handler;
import android.os.Looper;


public class HandlerTimer implements Runnable, ITimer {
    private Handler mHandler;
    private long mInterval;
    private TimerStatus mStatus;
    private Runnable mTask;

    private long mStartTS = 0L;

    public HandlerTimer(Runnable task) {
        this(1000, task);
    }

    public HandlerTimer(long interval, Runnable task) {
        this(interval, task, new Handler(Looper.getMainLooper()));
    }

    public HandlerTimer(long interval, Runnable task, Handler handler) {
        if (handler == null || task == null) {
            throw new NullPointerException("handler or task must not be null");
        }
        this.mInterval = interval;
        this.mTask = task;
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

        mTask.run();

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

    /**
     * Timer status
     */
    public enum TimerStatus {

        Waiting(0, "Wating"),
        Running(1, "Running"),
        Paused(-1, "Paused"),
        Stopped(-2, "Stopped");

        private int code;
        private String desc;

        private TimerStatus(int code, String desc) {
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

}
