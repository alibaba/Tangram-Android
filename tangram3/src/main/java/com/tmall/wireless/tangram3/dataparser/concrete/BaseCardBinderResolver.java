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

import android.support.annotation.NonNull;

import com.tmall.wireless.tangram3.core.protocol.LayoutBinderResolver;
import com.tmall.wireless.tangram3.util.Preconditions;

/**
 * Created by villadora on 15/8/24.
 */
public class BaseCardBinderResolver extends LayoutBinderResolver<Card, BaseLayoutBinder> {


    @NonNull
    private CardResolver mDelegate;

    public BaseCardBinderResolver(@NonNull CardResolver delegate) {
        this.mDelegate = Preconditions.checkNotNull(delegate, "delegate resolver should not be null");
    }

    @Override
    public int size() {
        return mDelegate.size();
    }

    @Override
    public String type(BaseLayoutBinder gen) {
        throw new UnsupportedOperationException("BaseCardBinderResolver doesn't support check type by controller");
    }

    @NonNull
    public CardResolver getDelegate() {
        return mDelegate;
    }

    @Override
    public BaseLayoutBinder create(String type) {
        if (mDelegate.hasType(type)) {
            return new BaseLayoutBinder();
        }
        return null;
    }

    @Override
    public void register(String type, BaseLayoutBinder gen) {
        throw new UnsupportedOperationException("BaseCardBinderResolver doesn't support register new type");
    }
}