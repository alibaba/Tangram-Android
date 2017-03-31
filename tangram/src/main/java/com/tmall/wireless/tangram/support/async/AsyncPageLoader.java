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

package com.tmall.wireless.tangram.support.async;

import android.support.annotation.NonNull;

import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;

import java.util.List;

/**
 * Interface to provide load data async for a Card. The difference between this and {@link AsyncLoader} is that this one loads data with page which means the card has a long list of cell data that could not be loaded at once.
 * See default {@link AsyncPageLoader.LoadedCallback} implement in {@link CardLoadSupport}
 * <br />
 * Created by villadora on 15/12/3.
 */
public interface AsyncPageLoader {

    /**
     * start to load data for a card with paging
     * @param page page index
     * @param card the card who's cells is empty and need to be loaded
     * @param callback called after loading finished
     */
    void loadData(int page, @NonNull final Card card, @NonNull final LoadedCallback callback);

    interface LoadedCallback {

        /**
         * notify the card that loading has been finished and
         * @param hasMore true, there's more data, false, no more data
         */
        void finish(boolean hasMore);

        /**
         * notify the card that loading data is success and append data to card
         *
         * @param cells the cell list parsed from async loaded data
         * @param hasMore true, there's more data, false, no more data
         */
        void finish(List<BaseCell> cells, boolean hasMore);

        /**
         * notify the card that loading data is fail
         *
         * @param retry true, retry loading data later because there may be more data; false, no more data.
         */
        void fail(boolean retry);
    }
}
