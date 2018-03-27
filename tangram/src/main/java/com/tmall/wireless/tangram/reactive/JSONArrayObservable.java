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

package com.tmall.wireless.tangram.reactive;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.Nullable;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.observers.BasicQueueDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import org.json.JSONArray;

/**
 * Created by longerian on 2018/3/27.
 *
 * @author longerian
 * @date 2018/03/27
 */

public class JSONArrayObservable<T> extends Observable<T> {

    public static <T> Observable<T> fromJsonArray(JSONArray json) {
        ObjectHelper.requireNonNull(json, "items is null");
        if (json.length() == 0) {
            return empty();
        }
        return RxJavaPlugins.onAssembly(new JSONArrayObservable<T>(json));
    }

    private final JSONArray mJson;

    private JSONArrayObservable(JSONArray mJson) {
        this.mJson = mJson;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        FromJsonArrayDisposable<T> d = new FromJsonArrayDisposable<T>(observer, mJson);
        observer.onSubscribe(d);
        if (d.fusionMode) {
            return;
        }
        d.run();
    }

    static final class FromJsonArrayDisposable<T> extends BasicQueueDisposable<T> {

        final Observer<? super T> actual;

        final JSONArray array;

        int index;

        boolean fusionMode;

        volatile boolean disposed;

        FromJsonArrayDisposable(Observer<? super T> actual, JSONArray array) {
            this.actual = actual;
            this.array = array;
        }

        @Override
        public int requestFusion(int mode) {
            if ((mode & SYNC) != 0) {
                fusionMode = true;
                return SYNC;
            }
            return NONE;
        }

        @Nullable
        @Override
        public T poll() {
            int i = index;
            JSONArray a = array;
            if (i != a.length()) {
                index = i + 1;
                T value = null;
                try {
                    value = (T) a.opt(i);
                } catch (ClassCastException e) {
                    return null;
                }
                return ObjectHelper.requireNonNull(value, "The array element is null");
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return index == array.length();
        }

        @Override
        public void clear() {
            index = array.length();
        }

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        void run() {
            JSONArray a = array;
            int n = a.length();

            for (int i = 0; i < n && !isDisposed(); i++) {
                T value = (T) a.opt(i);
                if (value == null) {
                    actual.onError(new NullPointerException("The " + i + "th element is null"));
                    return;
                }
                actual.onNext(value);
            }
            if (!isDisposed()) {
                actual.onComplete();
            }
        }
    }

}
