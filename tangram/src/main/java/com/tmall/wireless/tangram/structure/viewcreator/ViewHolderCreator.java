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

package com.tmall.wireless.tangram.structure.viewcreator;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tmall.wireless.tangram.core.R;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.util.LogUtils;

import java.lang.reflect.Constructor;

/**
 * Created by mikeafc on 16/5/26.
 */
public class ViewHolderCreator<T extends ViewHolderCreator.ViewHolder, V extends View> {

    public static final String TAG = "ViewHolderCreator";

    public final int mLayoutResId;
    public final Class<T> mClz;
    public final Class<V> viewClz;

    public ViewHolderCreator(@LayoutRes int layoutResId, Class<T> clz, Class<V> viewClz) {
        this.mLayoutResId = layoutResId;
        this.mClz = clz;
        this.viewClz = viewClz;
    }

    public V create(@NonNull Context context, ViewGroup parent) {
        View rootView = LayoutInflater.from(context).inflate(mLayoutResId, parent, false);

        try {
            V view = viewClz.cast(rootView);
            Constructor<T> constructor = mClz.getConstructor(Context.class);
            T holder = constructor.newInstance(context);
            holder.onRootViewCreated(view);
            view.setTag(R.id.TANGRAM_VIEW_HOLDER_TAG, holder);
            return view;
        } catch (Exception e) {
            if (TangramBuilder.isPrintLog()) {
                LogUtils.e(TAG, "Exception when inflate layout: " + context.getResources().getResourceName(mLayoutResId) + " stack: " + Log.getStackTraceString(e), e);
            }
        }
        return null;

    }

    public static ViewHolderCreator.ViewHolder getViewHolderFromView(@NonNull View view) {
        Object holder = view.getTag(R.id.TANGRAM_VIEW_HOLDER_TAG);
        if (holder instanceof ViewHolder) {
            return (ViewHolder) holder;
        }
        return null;
    }

    public abstract static class ViewHolder {

        protected final Context mContext;

        public ViewHolder(Context context) {
            this.mContext = context;
        }

        protected abstract void onRootViewCreated(View view);
    }
}
