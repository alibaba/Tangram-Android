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

import java.util.ArrayList;
import java.util.List;

import android.support.v4.util.ArrayMap;
import com.tmall.wireless.tangram.ext.BannerListener;
import com.tmall.wireless.tangram.support.RxBannerScrolledListener.ScrollEvent;
import io.reactivex.Observable;

/**
 * You can get banner scroll info from this support
 * Created by mikeafc on 17/4/9.
 */
public class BannerSupport {

    private ArrayMap<String, BannerListener> mSelectedListenerArrayMap = new ArrayMap<>();
    private ArrayMap<String, BannerSelectedObservable> mSelectedObservableArrayMap = new ArrayMap<>();

    private ArrayMap<String, BannerListener> mScrolledListenerArrayMap = new ArrayMap<>();
    private ArrayMap<String, BannerScrolledObservable> mScrolledObservableArrayMap = new ArrayMap<>();

    private ArrayMap<String, BannerListener> mScrollStateListenerArrayMap = new ArrayMap<>();
    private ArrayMap<String, BannerScrollStateChangedObservable> mScrollStateObservableArrayMap = new ArrayMap<>();

    @Deprecated
    private List<BannerListener> listeners = new ArrayList<BannerListener>();

    @Deprecated
    public void registerPageChangeListener(BannerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Deprecated
    public void unregisterPageChangeListener(BannerListener listener) {
        listeners.remove(listener);
    }

    @Deprecated
    public List<BannerListener> getListeners() {
        return listeners;
    }

    public BannerListener getSelectedListenerById(String id) {
        return mSelectedListenerArrayMap.get(id);
    }

    public BannerListener getScrolledListenerById(String id) {
        return mScrolledListenerArrayMap.get(id);
    }

    public BannerListener getScrollStateChangedListenerById(String id) {
        return mScrollStateListenerArrayMap.get(id);
    }

    public Observable<Integer> observeSelected(String id) {
        BannerListener rxBannerSelectedListener = mSelectedListenerArrayMap.get(id);
        if (rxBannerSelectedListener == null) {
            rxBannerSelectedListener = new RxBannerSelectedListener();
            mSelectedListenerArrayMap.put(id, rxBannerSelectedListener);
        }
        BannerSelectedObservable bannerSupportObservable = mSelectedObservableArrayMap.get(id);
        if (bannerSupportObservable == null) {
            bannerSupportObservable = new BannerSelectedObservable((RxBannerSelectedListener)rxBannerSelectedListener);
            bannerSupportObservable.share();
        }
        return bannerSupportObservable;
    }

    public Observable<ScrollEvent> observeScrolled(String id) {
        BannerListener rxBannerSelectedListener = mScrolledListenerArrayMap.get(id);
        if (rxBannerSelectedListener == null) {
            rxBannerSelectedListener = new RxBannerScrolledListener();
            mScrolledListenerArrayMap.put(id, rxBannerSelectedListener);
        }
        BannerScrolledObservable bannerSupportObservable = mScrolledObservableArrayMap.get(id);
        if (bannerSupportObservable == null) {
            bannerSupportObservable = new BannerScrolledObservable((RxBannerScrolledListener)rxBannerSelectedListener);
            bannerSupportObservable.share();
        }
        return bannerSupportObservable;
    }

    public Observable<Integer> observeScrollStateChanged(String id) {
        BannerListener rxBannerSelectedListener = mScrollStateListenerArrayMap.get(id);
        if (rxBannerSelectedListener == null) {
            rxBannerSelectedListener = new RxBannerScrollStateChangedListener();
            mScrollStateListenerArrayMap.put(id, rxBannerSelectedListener);
        }
        BannerScrollStateChangedObservable bannerSupportObservable = mScrollStateObservableArrayMap.get(id);
        if (bannerSupportObservable == null) {
            bannerSupportObservable = new BannerScrollStateChangedObservable((RxBannerScrollStateChangedListener)rxBannerSelectedListener);
            bannerSupportObservable.share();
        }
        return bannerSupportObservable;
    }

    public void destroy() {
        destroyBannerSelected();
        destroyBannerScrolled();
        destroyBannerScrollStateChanged();
    }

    public void destroyBannerSelected() {
        mSelectedListenerArrayMap.clear();
        mSelectedObservableArrayMap.clear();

    }

    public void destroyBannerScrolled() {
        mScrolledListenerArrayMap.clear();
        mScrolledObservableArrayMap.clear();

    }

    public void destroyBannerScrollStateChanged() {
        mScrollStateListenerArrayMap.clear();
        mScrollStateObservableArrayMap.clear();
    }

}