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

import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.tmall.wireless.tangram3.support.HandlerTimer.TimerStatus;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * Created by villadora on 15/9/8.
 */
public class TimerSupport {

    private static final int MILLISECOND = 1000;

    private ITimer mDefaultTimer = new HandlerTimer(MILLISECOND);

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
        mDefaultTimer.register(interval, onTickListener, intermediate);
    }

    /**
     * unregister a onTickListener
     * @param onTickListener
     */
    public void unregister(@NonNull OnTickListener onTickListener) {
        mDefaultTimer.unregister(onTickListener);
    }

    /**
     * @return current timer status
     */
    @Keep
    public TimerStatus getStatus() {
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
        mDefaultTimer.stop();
        mDefaultTimer.clear();
    }

    public boolean isRegistered(@NonNull OnTickListener onTickListener) {
        return mDefaultTimer.isRegistered(onTickListener);
    }


    /**
     * tick lister, called at every registered interval time passed
     */
    public interface OnTickListener {
        void onTick();
    }

    /**
     * return tick observable for each user, user should handle the observable with cell's lifecycle
     * @param interval timer interval, in TimeUnit.SECOND
     * @param intermediate true, get event immediately
     * @return
     * @since 3.0.0
     */
    public Observable<Long> getTickObservable(int interval, boolean intermediate) {
        return Observable.interval(intermediate ? 0 : interval, interval, TimeUnit.SECONDS);
    }

    /**
     * return tick observable for each user, user should handle the observable with cell's lifecycle
     * @param interval timer interval, in TimeUnit.SECOND
     * @param total total event count
     * @param intermediate true, get event immediately
     * @return
     * @since 3.0.0
     */
    public Observable<Long> getTickObservable(int interval, int total, boolean intermediate) {
        return Observable.intervalRange(0, total, intermediate ? 0 : interval, interval, TimeUnit.SECONDS);
    }

}
