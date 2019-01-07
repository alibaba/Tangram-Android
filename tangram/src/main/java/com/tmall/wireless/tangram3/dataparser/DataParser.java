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

package com.tmall.wireless.tangram3.dataparser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tmall.wireless.tangram3.core.service.ServiceManager;
import com.tmall.wireless.tangram3.dataparser.concrete.Card;
import com.tmall.wireless.tangram3.dataparser.concrete.ComponentInfo;
import com.tmall.wireless.tangram3.dataparser.concrete.Style;
import com.tmall.wireless.tangram3.structure.BaseCell;

import java.util.List;
import java.util.Map;

/**
 * DataParser parse data into structures
 */
public abstract class DataParser<O, T> {

    @NonNull
    public abstract List<Card> parseGroup(@Nullable T data, ServiceManager serviceManager);

    @NonNull
    public abstract List<BaseCell> parseComponent(@Nullable T data, Card parent, ServiceManager serviceManager, Map<String, ComponentInfo> componentInfoMap);

    @NonNull
    public abstract Card parseSingleGroup(@Nullable O data, ServiceManager serviceManager);

    @NonNull
    public abstract BaseCell parseSingleComponent(@Nullable O data, Card parent, ServiceManager serviceManager, Map<String, ComponentInfo> componentInfoMap);

    @NonNull
    public abstract <T extends Style> T parseStyle(@NonNull T style, @Nullable O data);
}
