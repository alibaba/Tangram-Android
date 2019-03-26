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

package com.tmall.wireless.tangram3.core.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.wireless.tangram3.core.protocol.ControlBinder;

/**
 * Created by villadora on 15/8/19.
 */
public class BinderViewHolder<C, V extends View> extends RecyclerView.ViewHolder implements VirtualLayoutManager.CacheViewHolder {

    public ControlBinder<C, V> controller;

    public V itemView;

    public C data;

    public BinderViewHolder(V itemView, @NonNull ControlBinder<C, V> binder) {
        super(itemView);
        this.itemView = itemView;
        this.controller = binder;

    }

    /**
     * Bind data to inner view
     *
     * @param data
     */
    public void bind(C data) {
        if (itemView == null || controller == null) {
            return;
        }
        this.controller.mountView(data, itemView);
        this.data = data;
    }

    /**
     * unbind the data, make the view re-usable
     */
    public void unbind() {
        if (itemView == null || controller == null) {
            return;
        }
        if (data != null) {
            this.controller.unmountView(data, itemView);
        }
    }

    @Override
    public boolean needCached() {
        if (data instanceof CacheItem) {
            return ((CacheItem) data).isStableCache();
        }

        return false;
    }
}
