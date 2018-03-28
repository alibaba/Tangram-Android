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

package com.tmall.wireless.tangram.dataparser;

import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.op.ParseComponentsOp;
import com.tmall.wireless.tangram.op.ParseGroupsOp;
import com.tmall.wireless.tangram.op.ParseSingleComponentOp;
import com.tmall.wireless.tangram.op.ParseSingleGroupOp;
import com.tmall.wireless.tangram.op.TangramOp2;
import com.tmall.wireless.tangram.op.TangramOp3;
import io.reactivex.ObservableTransformer;

/**
 * DataParser parse data into structures
 */
public abstract class DataParser<O, T, C, L> {

    @NonNull
    public abstract List<C> parseGroup(@Nullable T data, ServiceManager serviceManager);

    @NonNull
    public abstract List<L> parseComponent(@Nullable T data, ServiceManager serviceManager);

    /**
     *
     * @param data
     * @param parent
     * @param serviceManager
     * @return
     * @since 3.0.0
     */
    @NonNull
    public abstract List<L> parseComponent(@Nullable T data, C parent, ServiceManager serviceManager);

    /**
     *
     * @param data
     * @param serviceManager
     * @return
     * @since 3.0.0
     */
    @NonNull
    public abstract C parseSingleGroup(@Nullable O data, ServiceManager serviceManager);

    /**
     *
     * @param data
     * @param parent
     * @param serviceManager
     * @return
     * @since 3.0.0
     */
    @NonNull
    public abstract L parseSingleComponent(@Nullable O data, C parent, ServiceManager serviceManager);

    /**
     *
     * @return
     * @since 3.0.0
     */
    @NonNull
    public abstract ObservableTransformer<ParseGroupsOp, List<C>> getGroupTransformer();

    /**
     *
     * @return
     * @since 3.0.0
     */
    @NonNull
    public abstract ObservableTransformer<ParseComponentsOp, List<L>> getComponentTransformer();

    /**
     *
     * @return
     * @since 3.0.0
     */
    @NonNull
    public abstract ObservableTransformer<ParseSingleGroupOp, C> getSingleGroupTransformer();

    /**
     *
     * @return
     * @since 3.0.0
     */
    @NonNull
    public abstract ObservableTransformer<ParseSingleComponentOp, L> getSingleComponentTransformer();
}
