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

package com.tmall.wireless.tangram.dataparser.concrete;

import com.tmall.wireless.tangram.TangramBuilder;

/**
 * Methods to control components' lifecycle
 */
public abstract class ComponentLifecycle {

    public boolean mIsActivated = false;

    /*
     * Manage card lifecycle
     */
    public void added() {
        if (mIsActivated && TangramBuilder.isPrintLog()) {
            throw new IllegalStateException("Component can not be added more than once");
        }

        mIsActivated = true;

        onAdded();
    }

    public void removed() {
        if (!mIsActivated && TangramBuilder.isPrintLog()) {
            throw new IllegalStateException("Component can not be removed more than once");
        }

        mIsActivated = false;

        onRemoved();
    }


    protected void onAdded() {

    }

    protected void onRemoved() {

    }

}
