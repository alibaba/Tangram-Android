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

import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;

import java.util.List;

/**
 * A helper class supports loading data of a card
 */
public class CardLoadSupport {

    private static int sInitialPage = 1;


    public static void setInitialPage(int initialPage) {
        sInitialPage = initialPage;
    }

    private AsyncPageLoader mAsyncPageLoader;

    private AsyncLoader mAsyncLoader;

    public CardLoadSupport() {}

    /**
     * construct with a custom {@link AsyncLoader}
     * @param loader custom loader
     */
    public CardLoadSupport(AsyncLoader loader) {
        this(loader, null);
    }

    /**
     * construct with a custom {@link AsyncPageLoader}
     * @param loader custom page loader
     */
    public CardLoadSupport(AsyncPageLoader loader) {
        this(null, loader);
    }

    /**
     * construct with a custom {@link AsyncLoader} and a custom {@link AsyncPageLoader}
     * @param loader custom loader
     * @param pageLoader custom page loader
     */
    public CardLoadSupport(AsyncLoader loader, AsyncPageLoader pageLoader) {
        this.mAsyncLoader = loader;
        this.mAsyncPageLoader = pageLoader;

    }

    public void replaceLoader(AsyncLoader loader) {
        this.mAsyncLoader = loader;
    }

    public void replaceLoader(AsyncPageLoader pageLoader) {
        this.mAsyncPageLoader = pageLoader;
    }

    public void replaceLoader(AsyncLoader loader, AsyncPageLoader pageLoader) {
        this.mAsyncLoader = loader;
        this.mAsyncPageLoader = pageLoader;
    }

    /**
     * start load data for a card, usually called by {@link com.tmall.wireless.tangram.TangramEngine}
     * @param card the card need async loading data
     */
    public void doLoad(final Card card) {
        if (mAsyncLoader == null)
            return;
        if (!card.loading && !card.loaded) {
            card.loading = true;
            mAsyncLoader.loadData(card, new AsyncLoader.LoadedCallback() {
                @Override
                public void finish() {
                    card.loading = false;
                    card.loaded = true;
                }

                @Override
                public void finish(List<BaseCell> cells) {
                    finish();
                    card.setCells(cells);
                    card.notifyDataChange();
                }

                public void fail(boolean loaded) {
                    card.loading = false;
                    card.loaded = loaded;
                }
            });
        }
    }

    /**
     * start load data with paging for a card, usually called by {@link com.tmall.wireless.tangram.TangramEngine}
     * @param card the card need async loading data
     */
    public void loadMore(final Card card) {
        if (mAsyncPageLoader == null)
            return;
        if (!card.loading && card.loadMore && card.hasMore) {
            card.loading = true;
            if (!card.loaded)
                card.page = sInitialPage;


            mAsyncPageLoader.loadData(card.page, card, new AsyncPageLoader.LoadedCallback() {
                @Override
                public void finish(boolean hasMore) {
                    card.loaded = true;
                    card.loading = false;
                    card.page++;
                    card.hasMore = hasMore;
                }

                @Override
                public void finish(List<BaseCell> cells, boolean hasMore) {
                    if (card.page == sInitialPage) {
                        card.setCells(cells);
                    } else
                        card.addCells(cells);

                    finish(hasMore);
                    card.notifyDataChange();
                }

                @Override
                public void fail(boolean retry) {
                    card.loaded = true;
                    card.loading = false;
                    card.hasMore = retry;
                }
            });
        }
    }
}
