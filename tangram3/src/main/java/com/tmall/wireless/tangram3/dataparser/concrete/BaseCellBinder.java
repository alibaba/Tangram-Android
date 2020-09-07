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

package com.tmall.wireless.tangram3.dataparser.concrete;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.tmall.wireless.tangram3.MVHelper;
import com.tmall.wireless.tangram3.core.protocol.ControlBinder;
import com.tmall.wireless.tangram3.structure.BaseCell;
import com.tmall.wireless.tangram3.structure.ViewCreator;
import com.tmall.wireless.tangram3.structure.viewcreator.ViewHolderCreator;
import com.tmall.wireless.tangram3.util.Preconditions;

/**
 *
 */
public class BaseCellBinder<T extends ViewHolderCreator.ViewHolder, V extends View> implements ControlBinder<BaseCell, V> {

    private static final String TAG = "BaseCellBinder";

    private ViewCreator<V> mViewCreator;

    private ViewHolderCreator<T, V> viewHolderCreator;

    @NonNull
    private MVHelper mMvHelper;

    private String type;

    public BaseCellBinder(@NonNull Class<V> viewClz, @NonNull MVHelper mvHelper) {
        this.mViewCreator = new ViewCreator<>(viewClz);
        this.mMvHelper = Preconditions.checkNotNull(mvHelper, "mvHelper should not be null");
    }

    public BaseCellBinder(@NonNull ViewHolderCreator<T, V> viewHolderCreator, @NonNull MVHelper mvHelper) {
        this.viewHolderCreator = viewHolderCreator;
        this.mMvHelper = mvHelper;
    }

    public BaseCellBinder(String type, @NonNull MVHelper mvHelper) {
        this.type = type;
        this.mMvHelper = mvHelper;
    }

    @NonNull
    @Override
    public V createView(Context context, ViewGroup parent, ComponentInfo info) {
        V v;
        if (viewHolderCreator != null) {
            v = viewHolderCreator.create(context, parent);
        } else if (mViewCreator != null) {
            v = mViewCreator.create(context, parent);
        } else {
            v = (V) mMvHelper.renderManager().createView(context, parent, info);
        }

        return v;
    }

    @Override
    public void mountView(@NonNull BaseCell data, @NonNull V view) {
        mMvHelper.mountView(data, view);
    }

    @Override
    public void unmountView(@NonNull BaseCell data, @NonNull V view) {
        mMvHelper.unMountView(data, view);
    }
}
