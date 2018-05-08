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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import com.tmall.wireless.tangram.TangramEngine;
import com.tmall.wireless.tangram.structure.BaseCell;

/**
 * Created by longerian on 17/1/22.
 */
@Deprecated
abstract public class PageDetectorSupport {

    protected TangramEngine mTangramEngine;

    private Context mContext;

    private boolean isDetectingPageAppear;

    private boolean isDetectingFastScroll;

    private int idleInterval;

    private final boolean isAutoDetectIdle;

    private int fastScrollThreshold;

    private long lastAnchorTime;

    private Application.ActivityLifecycleCallbacks mLifecycleCallbacks
        = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            Context host = mTangramEngine.getContext();
            if (host == activity) {
                startDetectPage();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Context host = mTangramEngine.getContext();
            if (host == activity) {
                stopDetectPage();
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    private TimerSupport.OnTickListener mOnTickListener = new TimerSupport.OnTickListener() {

        @Override
        public void onTick() {
            ((Application) mContext.getApplicationContext())
                .unregisterActivityLifecycleCallbacks(mLifecycleCallbacks);
            stopDetectPage();
            onInternalPageIdle();
        }
    };

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        boolean disable;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (!disable) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (isAutoDetectIdle) {
                        ((Application) recyclerView.getContext().getApplicationContext())
                            .registerActivityLifecycleCallbacks(mLifecycleCallbacks);
                        startDetectPage();
                    }
                    isDetectingFastScroll = true;
                    disable = true;
                }
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                onScrollStateIdle();
            }
        }
    };

    /**
     * @param idleInterval        页面停留时长，单位ms
     * @param fastScrollThreshold 一秒内快速滚动的个数阈值
     */
    public PageDetectorSupport(TangramEngine tangramEngine, int idleInterval,
        boolean isAutoDetectIdle, int fastScrollThreshold) {
        this.mTangramEngine = tangramEngine;
        this.idleInterval = idleInterval;
        this.isAutoDetectIdle = isAutoDetectIdle;
        this.fastScrollThreshold = fastScrollThreshold;
        this.mContext = mTangramEngine.getContext();
    }

    abstract protected void onInternalPageIdle();

    abstract protected void onInternalPageFastScroll();

    public void onBindItem(int position, BaseCell cell) {

    }

    final public void onBindItem(int position, boolean showFromStart, BaseCell cell) {
        if (isDetectingFastScroll) {
            if (position % fastScrollThreshold == 0) {
                long now = System.currentTimeMillis();
                if (now - lastAnchorTime < 1000) {
                    mOnScrollListener = null;
                    isDetectingFastScroll = false;
                    onInternalPageFastScroll();
                }
                lastAnchorTime = System.currentTimeMillis();
            }
        }
        onBindItem(position, cell);
    }

    public void startDetectFastScroll() {
        if (mOnScrollListener != null) {
            RecyclerView recyclerView = mTangramEngine.getContentView();
            if (recyclerView != null) {
                recyclerView.removeOnScrollListener(mOnScrollListener);
                recyclerView.setOnScrollListener(mOnScrollListener);
            }
        }
    }

    public void startDetectPage() {
        if (!isDetectingPageAppear) {
            TimerSupport timerSupport = mTangramEngine.getService(TimerSupport.class);
            int tickInterval = idleInterval / 1000;
            if (tickInterval != 0 && timerSupport.isRegistered(mOnTickListener)) {
                timerSupport.register(idleInterval / 1000, mOnTickListener);
            }
            isDetectingPageAppear = true;
        }
    }

    public void stopDetectPage() {
        if (isDetectingPageAppear) {
            TimerSupport timerSupport = mTangramEngine.getService(TimerSupport.class);
            timerSupport.unregister(mOnTickListener);
            isDetectingPageAppear = false;
        }
    }

    public void onDestroy() {
        if (isAutoDetectIdle) {
            ((Application) this.mContext.getApplicationContext())
                .unregisterActivityLifecycleCallbacks(mLifecycleCallbacks);
        }
    }

    public void setIdleInterval(int idleInterval) {
        this.idleInterval = idleInterval;
    }

    public void setFastScrollThreshold(int fastScrollThreshold) {
        this.fastScrollThreshold = fastScrollThreshold;
    }

    public int getIdleInterval() {
        return idleInterval;
    }

    public int getFastScrollThreshold() {
        return fastScrollThreshold;
    }

    public void onScrollStateIdle() {
        ;
    }

}
