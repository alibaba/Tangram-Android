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
 * Interface to provide load data async for a Card. See default {@link LoadedCallback} implement in {@link CardLoadSupport}
 * <br />
 * Created by villadora on 15/12/4.
 */
public interface AsyncLoader {

    /**
     * start to load data for a card
     * @param card the card who's cells is empty and need to be loaded
     * @param callback called after loading finished
     */
    void loadData(final Card card, @NonNull final LoadedCallback callback);

    interface LoadedCallback {

        /**
         * notify the card that loading has been finished
         */
        void finish();

        /**
         * notify the card that loading has been finished with failure.
         * @param loaded true, tell the card that there's no need to try again, false, tell the card to try again
         */
        void fail(boolean loaded);

        /**
         * notify the card that loading data is success and set data to card
         *
         * @param cells the cell list parsed from async loaded data
         */
        void finish(List<BaseCell> cells);
    }
}
