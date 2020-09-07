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

import android.support.v4.util.ArrayMap;

import com.tmall.wireless.tangram3.ext.BannerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * You can get banner scroll info from this support
 * Created by mikeafc on 17/4/9.
 */
public class BannerSupport {

    private ArrayMap<String, List<BannerListener>> mSelectedListenerArrayMap = new ArrayMap<>();

    private ArrayMap<String, List<BannerListener>> mScrolledListenerArrayMap = new ArrayMap<>();

    private ArrayMap<String, List<BannerListener>> mScrollStateListenerArrayMap = new ArrayMap<>();

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

    public List<BannerListener> getSelectedListenerById(String id) {
        return mSelectedListenerArrayMap.get(id);
    }

    public List<BannerListener> getScrolledListenerById(String id) {
        return mScrolledListenerArrayMap.get(id);
    }

    public List<BannerListener> getScrollStateChangedListenerById(String id) {
        return mScrollStateListenerArrayMap.get(id);
    }

    public void destroy() {
        destroyBannerSelected();
        destroyBannerScrolled();
        destroyBannerScrollStateChanged();
    }

    public void destroyBannerSelected() {
        mSelectedListenerArrayMap.clear();
    }

    public void destroyBannerScrolled() {
        mScrolledListenerArrayMap.clear();
    }

    public void destroyBannerScrollStateChanged() {
        mScrollStateListenerArrayMap.clear();
    }

}