/*
 * MIT License
 *
 * Copyright (c) 2017 Alibaba Group
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

package com.tmall.wireless.tangram.core.resolver;

import android.support.v4.util.ArrayMap;
import android.util.SparseArray;

import java.util.Map;

/**
 * Created by villadora on 15/8/23.
 */
public abstract class BaseResolver<T, O> implements Resolver<T, O> {

    protected ArrayMap<T, String> mMap = new ArrayMap<>(64);

    protected ArrayMap<String, T> mSparseArray = new ArrayMap<>(64);

    @Override
    public int size() {
        return mSparseArray.size();
    }

    @Override
    public String type(T gen) {
        if (mMap.containsKey(gen)) {
            return mMap.get(gen);
        }
        return UNKNOWN;
    }


    @Override
    public void register(String type, T gen) {
        mMap.put(gen, type);
        mSparseArray.put(type, gen);
    }

    @Override
    public boolean has(String type) {
        return mSparseArray.containsKey(type);
    }
}
