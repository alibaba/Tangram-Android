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

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.wireless.tangram3.MVHelper;
import com.tmall.wireless.tangram3.core.service.ServiceManager;
import com.tmall.wireless.tangram3.dataparser.IAdapterBuilder;
import com.tmall.wireless.tangram3.structure.BaseCell;

/**
 * Created by villadora on 15/8/24.
 */
public class PojoAdapterBuilder implements IAdapterBuilder<Card, BaseCell> {


    @Override
    public PojoGroupBasicAdapter newAdapter(
            @NonNull final Context context, @NonNull final VirtualLayoutManager layoutManager,
            @NonNull final ServiceManager serviceManager) {

        BaseCellBinderResolver componentBinderResolver = serviceManager.getService(BaseCellBinderResolver.class);
        BaseCardBinderResolver cardBinderResolver = serviceManager.getService(BaseCardBinderResolver.class);
        MVHelper mvHelper = serviceManager.getService(MVHelper.class);

        return new PojoGroupBasicAdapter(context, layoutManager, componentBinderResolver,
                cardBinderResolver, mvHelper);
    }
}

