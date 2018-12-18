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

package com.tmall.wireless.tangram3.support.async;

import com.tmall.wireless.tangram3.TangramEngine;
import com.tmall.wireless.tangram3.dataparser.concrete.Card;
import com.tmall.wireless.tangram3.op.LoadGroupOp;
import com.tmall.wireless.tangram3.op.LoadMoreOp;
import com.tmall.wireless.tangram3.structure.BaseCell;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

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
     * start load data for a card, usually called by {@link TangramEngine}
     * @param card the card need async loading data
     */
    public void doLoad(final Card card) {
        if (mAsyncLoader == null) {
            return;
        }
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
                    card.addCells(cells);
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
     * start load data with paging for a card, usually called by {@link TangramEngine}
     * @param card the card need async loading data
     */
    public void loadMore(final Card card) {
        if (mAsyncPageLoader == null) {
            return;
        }
        if (!card.loading && card.loadMore && card.hasMore) {
            card.loading = true;
            if (!card.loaded) {
                card.page = sInitialPage;
            }


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
                    } else {
                        card.addCells(cells);
                    }

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

    private Observable<Card> mLoadCardObservable;

    private Observable<Card> mLoadMoreCardObservable;

    private ObservableEmitter<Card> mLoadCardObserver;

    private ObservableEmitter<Card> mLoadMoreObserver;

    /**
     *
     * @return An observable start loading a card
     */
    public Observable<Card> observeCardLoading() {
        if (mLoadCardObservable == null) {
            mLoadCardObservable = Observable.create(new ObservableOnSubscribe<Card>() {
                @Override
                public void subscribe(ObservableEmitter<Card> emitter) throws Exception {
                    mLoadCardObserver = emitter;
                }
            }).filter(new Predicate<Card>() {
                @Override
                public boolean test(Card card) throws Exception {
                    return !card.loading && !card.loaded;
                }
            }).doOnNext(new Consumer<Card>() {
                @Override
                public void accept(Card card) throws Exception {
                    card.loading = true;
                }
            }).doOnDispose(new Action() {
                @Override
                public void run() throws Exception {
                    mLoadCardObserver = null;
                }
            });
        }
        return mLoadCardObservable;
    }

    /**
     * Start to load data for a card, usually called by {@link TangramEngine}
     * @param card the card need reactively loading data
     */
    public void reactiveDoLoad(Card card) {
        if (mLoadCardObserver == null) {
            return;
        }
        mLoadCardObserver.onNext(card);
    }

    /**
     * Combine with {@link #observeCardLoading()}, use this method as success consumer to subscribe to the Observable.<br />
     * If your request success, provide a {@link LoadGroupOp} with original card and non-empty List<BaseCell>.<br />
     * Otherwise, provide a {@link LoadGroupOp} with original card and empty List<BaseCell>.
     * @return A consumer to consume load event.
     */
    public Consumer<LoadGroupOp> asDoLoadFinishConsumer() {
        return new Consumer<LoadGroupOp>() {
            @Override
            public void accept(LoadGroupOp result) throws Exception {
                Card card = result.getArg1();
                card.loading = false;
                List<BaseCell> cells = result.getArg2();
                if (cells != null && !cells.isEmpty()) {
                    card.loaded = true;
                    card.setCells(cells);
                    card.notifyDataChange();
                } else {
                    card.loaded = false;
                }
            }
        };
    }

    /**
     *
     * @return An observable start loading more for a card
     */
    public Observable<Card> observeCardLoadingMore() {
        if (mLoadMoreCardObservable == null) {
            mLoadMoreCardObservable = Observable.create(new ObservableOnSubscribe<Card>() {
                @Override
                public void subscribe(ObservableEmitter<Card> emitter) throws Exception {
                    mLoadMoreObserver = emitter;
                }
            }).filter(new Predicate<Card>() {
                @Override
                public boolean test(Card card) throws Exception {
                    return !card.loading && card.loadMore && card.hasMore;
                }
            }).doOnNext(new Consumer<Card>() {
                @Override
                public void accept(Card card) throws Exception {
                    card.loading = true;
                    if (!card.loaded) {
                        card.page = sInitialPage;
                    }
                }
            }).doOnDispose(new Action() {
                @Override
                public void run() throws Exception {
                    mLoadMoreObserver = null;
                }
            });
        }
        return mLoadMoreCardObservable;
    }

    /**
     * Start to load more data for a card, usually called by {@link TangramEngine}
     * @param card the card need reactively loading data
     */
    public void reactiveDoLoadMore(Card card) {
        if (mLoadMoreObserver == null) {
            return;
        }
        mLoadMoreObserver.onNext(card);
    }


    /**
     * Combine with {@link #observeCardLoadingMore()}, use this method as success consumer to subscribe to the Observable.<br />
     * If your request success, provide a {@link LoadMoreOp} with original card, non-empty List<BaseCell> and boolean value to indicate whether there is more.<br />
     * Otherwise, provide a {@link LoadMoreOp} with original card, empty List<BaseCell> and boolean value to indicate whether there is more.
     * @return A consumer to consume load more event.
     */
    public Consumer<LoadMoreOp> asDoLoadMoreFinishConsumer() {
        return new Consumer<LoadMoreOp>() {
            @Override
            public void accept(LoadMoreOp result) throws Exception {
                Card card = result.getArg1();
                card.loading = false;
                card.loaded = true;
                List<BaseCell> cells = result.getArg2();
                boolean hasMore = result.getArg3();
                if (cells != null && !cells.isEmpty()) {
                    if (card.page == sInitialPage) {
                        card.setCells(cells);
                    } else {
                        card.addCells(cells);
                    }
                    card.page++;
                    card.hasMore = hasMore;
                    card.notifyDataChange();
                } else {
                    card.hasMore = hasMore;
                }
            }
        };
    }


}
