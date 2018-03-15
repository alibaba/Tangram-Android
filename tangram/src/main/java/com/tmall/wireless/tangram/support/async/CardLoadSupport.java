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

package com.tmall.wireless.tangram.support.async;

import android.support.v4.util.Pair;
import android.util.Log;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

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

    //FIXME cancel\ reset
    private ObservableEmitter<Card> mLoadCardObserver;

    private ObservableEmitter<Card> mLoadMoreObserver;

    /**
     *
     * @return An observable start loading a card
     */
    public Observable<Card> observeCardLoading() {
        Observable<Card> mLoadCardObservable = Observable.create(new ObservableOnSubscribe<Card>() {
            @Override
            public void subscribe(ObservableEmitter<Card> emitter) throws Exception {
                mLoadCardObserver = emitter;
            }
        }).filter(new Predicate<Card>() {
            @Override
            public boolean test(Card card) throws Exception {
                Log.d("Longer", "observeCardLoading in test filter");
                return !card.loading && !card.loaded;
            }
        }).doOnNext(new Consumer<Card>() {
            @Override
            public void accept(Card card) throws Exception {
                Log.d("Longer", "observeCardLoading in doOnNext");
                card.loading = true;
            }
        });
        return mLoadCardObservable;
    }

    /**
     * start load data for a card, usually called by {@link com.tmall.wireless.tangram.TangramEngine}
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
     * If your request success, provide a Pair with original card and non-empty List<BaseCell>.
     * Otherwise, provide a Pair with original card and empty List<BaseCell>.
     * @return A consumer to consume load event.
     */
    public Consumer<Pair<Card, List<BaseCell>>> asDoLoadFinishConsumer() {
        return new Consumer<Pair<Card, List<BaseCell>>>() {
            @Override
            public void accept(Pair<Card, List<BaseCell>> result) throws Exception {
                Card card = result.first;
                card.loading = false;
                List<BaseCell> cells = result.second;
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
        Observable<Card> mLoadMoreCardObservable = Observable.create(new ObservableOnSubscribe<Card>() {
            @Override
            public void subscribe(ObservableEmitter<Card> emitter) throws Exception {
                mLoadMoreObserver = emitter;
            }
        }).filter(new Predicate<Card>() {
            @Override
            public boolean test(Card card) throws Exception {
                Log.d("Longer", "observeCardLoadingMore in test filter");
                return !card.loading && card.loadMore && card.hasMore;
            }
        }).doOnNext(new Consumer<Card>() {
            @Override
            public void accept(Card card) throws Exception {
                Log.d("Longer", "observeCardLoadingMore in doOnNext");
                card.loading = true;
                if (!card.loaded) {
                    card.page = sInitialPage;
                }
            }
        });
        return mLoadMoreCardObservable;
    }

    /**
     * start load more data for a card, usually called by {@link com.tmall.wireless.tangram.TangramEngine}
     * @param card the card need reactively loading data
     */
    public void reactiveDoLoadMore(Card card) {
        if (mLoadMoreObserver == null) {
            return;
        }
        mLoadMoreObserver.onNext(card);
    }


    /**
     * Combine with {@link #observeCardLoadingMore()}, use this method as success consumer to subscribe to the Observable<br />
     * If your request success, provide a Pair with original card and another Pair with non-empty List<BaseCell> and boolean value to indicate if there is more.
     * Otherwise, provide a Pair with original card and another Pair with empty List<BaseCell> and boolean value to indicate if there is more.
     * @return A consumer to consume load more event.
     */
    public Consumer<Pair<Card, Pair<List<BaseCell>, Boolean>>> asDoLoadMoreFinishConsumer() {
        return new Consumer<Pair<Card, Pair<List<BaseCell>, Boolean>>>() {
            @Override
            public void accept(Pair<Card, Pair<List<BaseCell>, Boolean>> result) throws Exception {
                Card card = result.first;
                card.loading = false;
                card.loaded = true;
                Pair<List<BaseCell>, Boolean> data = result.second;
                List<BaseCell> cells = data.first;
                boolean hasMore = data.second;
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
