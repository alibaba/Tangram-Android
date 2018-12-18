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

package com.tmall.wireless.tangram3.core.resolver;

import android.util.Log;

import com.tmall.wireless.tangram3.TangramBuilder;

/**
 * Resolver based on class types, its' methods are not thread-safe.
 *
 * @author villadora
 */
public abstract class ClassResolver<T> extends BaseResolver<Class<? extends T>, T> {

    private static final String TAG = "ClassResolver";

    @Override
    public T create(String type) {
        Class<? extends T> clz = mSparseArray.get(type);
        if (clz != null) {
            try {
                return clz.newInstance();
            } catch (InstantiationException e) {
                if (TangramBuilder.isPrintLog())
                    Log.e(TAG, e.getMessage(), e);
            } catch (IllegalAccessException e) {
                if (TangramBuilder.isPrintLog())
                    Log.e(TAG, e.getMessage(), e);
            }
        } else if (TangramBuilder.isPrintLog()) {
            throw new TypeNotFoundException("Can not find type: " + type + " in ClassResolver");
        }
        return null;
    }


    static final class TypeNotFoundException extends RuntimeException {
        TypeNotFoundException(String message) {
            super(message);
        }
    }
}
