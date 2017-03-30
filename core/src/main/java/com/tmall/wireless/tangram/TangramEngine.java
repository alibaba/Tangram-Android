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

package com.tmall.wireless.tangram;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.android.vlayout.Range;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.wireless.tangram.dataparser.DataParser;
import com.tmall.wireless.tangram.dataparser.IAdapterBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.expression.ITangramExprParser;
import com.tmall.wireless.tangram.expression.TangramExpr;
import com.tmall.wireless.tangram.expression.TangramExprSupport;
import com.tmall.wireless.tangram.ext.PullFromEndListener;
import com.tmall.wireless.tangram.ext.SwipeItemTouchListener;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.support.ExposureSupport;
import com.tmall.wireless.tangram.support.SimpleClickSupport;
import com.tmall.wireless.tangram.support.async.CardLoadSupport;
import com.tmall.wireless.tangram.util.Predicate;

import org.json.JSONArray;

import java.util.Collections;
import java.util.List;

/**
 * Created by villadora on 15/8/24.
 *
 * The core of Tangram used to access data, bind view, register service.
 */
public class TangramEngine extends BaseTangramEngine<JSONArray, Card, BaseCell> implements Engine,
        ITangramExprParser {

    private static final int NO_SWIPE = -1;

    public TangramEngine(@NonNull Context context,
                         @NonNull DataParser<JSONArray, Card, BaseCell> dataParser,
                         @NonNull IAdapterBuilder<Card, ?> adapterBuilder) {
        super(context, dataParser, adapterBuilder);
        this.register(DataParser.class, dataParser);
        mTangramExprSupport = new TangramExprSupport();
        mTangramExprSupport.registerExprParser(TangramExprSupport.TANGRAM, this);
        this.register(TangramExprSupport.class, mTangramExprSupport);

    }

    private TangramExprSupport mTangramExprSupport;

    private Runnable updateRunnable;

    private int mPreLoadNumber = 5;

    private boolean mEnableAutoLoadMore = true;

    private boolean mEnableLoadFirstPageCard = true;

    public void enableAutoLoadMore(boolean enableAutoLoadMore) {
        this.mEnableAutoLoadMore = enableAutoLoadMore;
    }

    public void setPreLoadNumber(int preLoadNumber) {
        if (preLoadNumber >= 0)
            this.mPreLoadNumber = preLoadNumber;
        else
            this.mPreLoadNumber = 0;
    }

    private SwipeItemTouchListener mSwipeItemTouchListener = null;
    private int mSwipeCardActionEdge = -1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindView(@NonNull RecyclerView view) {
        super.bindView(view);

        mSwipeItemTouchListener = new SwipeItemTouchListener(view.getContext(), mGroupBasicAdapter, getContentView());
        if (mSwipeCardActionEdge != -1) {
            mSwipeItemTouchListener.setActionEdge(mSwipeCardActionEdge);
        }

        view.addOnItemTouchListener(mSwipeItemTouchListener);
    }

    public void setPullFromEndListener(PullFromEndListener listener) {
        if (mSwipeItemTouchListener != null) {
            mSwipeItemTouchListener.setPullFromEndListener(listener);
        }
    }

    public void setNoScrolling(boolean noScrolling) {
        getLayoutManager().setNoScrolling(noScrolling);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindView() {

        RecyclerView contentView = getContentView();

        if (contentView != null && mSwipeItemTouchListener != null) {
            contentView.removeOnItemTouchListener(mSwipeItemTouchListener);
            mSwipeItemTouchListener = null;
        }
        super.unbindView();
    }

    /**
     * Call this method in RecyclerView's scroll listener. Would trigger the preload of card's data.
     */
    public void onScrolled() {
        //  due to a bug in 21: https://code.google.com/p/android/issues/detail?id=162753, which cause getDecoratedStart() throws NullPointException
        // officially reported it has been fixed in v22

        final int lastPosition = getLayoutManager().findLastVisibleItemPosition();
        final int firstPosition = getLayoutManager().findFirstVisibleItemPosition();

        int lastCardIndex = -1;
        int firstCardIndex = -1;
        int position = lastPosition;
        //find the last visible item in card
        for (int i = lastPosition; i >= firstPosition; i--) {
            lastCardIndex = mGroupBasicAdapter.findCardIdxFor(i);
            if (lastCardIndex >= 0) {
                position = i;
                break;
            }
        }

        for (int i = firstCardIndex; i <= lastPosition; i++) {
            firstCardIndex = mGroupBasicAdapter.findCardIdxFor(i);
            if (firstCardIndex >= 0) {
                break;
            }
        }

        if (lastCardIndex < 0 || firstCardIndex < 0) return;

        final CardLoadSupport loadSupport = getService(CardLoadSupport.class);
        if (loadSupport == null) return;

        List<Card> cards = mGroupBasicAdapter.getGroups();
        //check the loadmore state of current card first  range is inclusive-exclusive
        Card current = cards.get(lastCardIndex);
        Pair<Range<Integer>, Card> pair = mGroupBasicAdapter.getCardRange(lastCardIndex);
        if (pair != null && position >= pair.first.getUpper() - mPreLoadNumber) {
            // async load
            if (!TextUtils.isEmpty(current.load) && current.loaded) {
                // page load
                if (current.loadMore) {
                    loadSupport.loadMore(current);
                }
                return;
            }
        }


        boolean loadedMore = false;

        for (int i = firstCardIndex; i < Math.min(lastCardIndex + mPreLoadNumber, cards.size()); i++) {
            Card c = cards.get(i);
            // async load
            if (!TextUtils.isEmpty(c.load) && !c.loaded) {
                // page load
                if (c.loadMore && !loadedMore) {
                    // only load one load more card
                    loadSupport.loadMore(c);
                    loadedMore = true;
                } else {
                    loadSupport.doLoad(c);
                }
                c.loaded = true;
            }
        }


        if (mEnableAutoLoadMore && mGroupBasicAdapter.getItemCount() - position < mPreLoadNumber) {
            loadMoreCard();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setData(@Nullable JSONArray data) {
        super.setData(data);
        loadFirstPageCard();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setData(@Nullable List<Card> data) {
        super.setData(data);
        loadFirstPageCard();
    }

    /**
     *
     * @param enable True to auto trigger loading data for card whose's position in within 0 to {{@link #mPreLoadNumber}}.
     */
    public void setEnableLoadFirstPageCard(boolean enable) {
        mEnableLoadFirstPageCard = enable;
    }

    /**
     * Loading data for card whose's position in within 0 to {{@link #mPreLoadNumber}}.
     */
    public void loadFirstPageCard() {
        if (!mEnableLoadFirstPageCard)
            return;

        final CardLoadSupport loadSupport = getService(CardLoadSupport.class);
        if (loadSupport == null) return;

        boolean loadedMore = false;

        List<Card> cards = mGroupBasicAdapter.getGroups();

        for (int i = 0; i < Math.min(mPreLoadNumber, cards.size()); i++) {
            Card card = cards.get(i);

            if (!TextUtils.isEmpty(card.load) && !card.loaded) {
                // page load
                if (card.loadMore && !loadedMore) {
                    // only load one load more card
                    loadSupport.loadMore(card);
                    loadedMore = true;
                } else {
                    loadSupport.doLoad(card);
                }
                card.loaded = true;
            }
        }
    }


    public void addSimpleClickSupport(@NonNull final SimpleClickSupport support) {
        register(SimpleClickSupport.class, support);
    }

    public void addCardLoadSupport(@NonNull final CardLoadSupport support) {
        register(CardLoadSupport.class, support);
    }

    public void addExposureSupport(@NonNull final ExposureSupport support) {
        register(ExposureSupport.class, support);
    }


    public void loadMoreCard() {
        CardLoadSupport loadSupport = getService(CardLoadSupport.class);
        if (loadSupport == null) return;

        List<Card> groups = findGroups(new Predicate<Card>() {
            @Override
            public boolean isMatch(Card data) {
                return data.loadMore && data.hasMore && !data.loading
                        && !TextUtils.isEmpty(data.load);
            }
        });

        if (groups.size() != 0) {
            loadSupport.loadMore(groups.get(groups.size() - 1));
        }
    }

    /**
     * @param id Target card's id.
     * @return Return the target card.
     */
    public Card findCardById(String id) {
        MVHelper MVHelper = getService(MVHelper.class);
        if (MVHelper == null)
            return null;

        return MVHelper.resolver().findCardById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh(final boolean layoutUpdated) {
        final RecyclerView contentView = getContentView();

        if (contentView == null) return;

        if (contentView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
            // contentView.stopScroll();
        }

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!contentView.isComputingLayout()) {
                    //to prevent notify update when recyclerView is in computingLayout  process
                    mGroupBasicAdapter.notifyUpdate(layoutUpdated);
                }
            }
        };
        contentView.post(updateRunnable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        this.refresh(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceCard(Card oldCard, Card newCard) {
        List<Card> groups = this.mGroupBasicAdapter.getGroups();
        int index = groups.indexOf(oldCard);
        if (index >= 0) {
            replaceData(index, Collections.singletonList(newCard));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceCells(Card parent, List<BaseCell> cells) {
        if (parent != null && cells != null) {
            parent.setCells(cells);
            parent.notifyDataChange();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scrollToPosition(Card card) {
        List<BaseCell> cells = card.getCells();
        if (cells.size() > 0) {
            BaseCell cell = cells.get(0);
            int pos = mGroupBasicAdapter.getComponents().indexOf(cell);
            if (pos > 0) {
                this.getContentView().scrollToPosition(pos);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scrollToPosition(BaseCell cell) {
        if (cell != null) {
            int pos = mGroupBasicAdapter.getComponents().indexOf(cell);
            if (pos > 0) {
                this.getContentView().scrollToPosition(pos);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void topPosition(Card card) {
        List<BaseCell> cells = card.getCells();
        if (cells.size() > 0) {
            BaseCell cell = cells.get(0);
            int pos = mGroupBasicAdapter.getComponents().indexOf(cell);
            if (pos > 0) {
                VirtualLayoutManager lm = getLayoutManager();
                View view = lm.findViewByPosition(pos);
                if (view != null) {
                    int top = lm.getDecoratedTop(view);
                    this.getContentView().scrollBy(0, top);
                } else
                    this.getContentView().scrollToPosition(pos);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void topPosition(BaseCell cell) {
        if (cell != null) {
            int pos = mGroupBasicAdapter.getComponents().indexOf(cell);
            if (pos > 0) {
                VirtualLayoutManager lm = getLayoutManager();
                View view = lm.findViewByPosition(pos);
                if (view != null) {
                    int top = lm.getDecoratedTop(view);
                    this.getContentView().scrollBy(0, top);
                } else {
                    this.getContentView().scrollToPosition(pos);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        RecyclerView contentView = getContentView();
        if (contentView != null) {
            contentView.removeCallbacks(updateRunnable);
        }
        super.destroy();
    }

    public void setSwipeCardActionEdge(int actionEdge) {
        if (actionEdge == NO_SWIPE) {
            if (mSwipeItemTouchListener != null)
                getContentView().removeOnItemTouchListener(mSwipeItemTouchListener);
        } else {
            mSwipeCardActionEdge = actionEdge;
            if (mSwipeItemTouchListener != null) {
                getContentView().removeOnItemTouchListener(mSwipeItemTouchListener);
                mSwipeItemTouchListener.setActionEdge(actionEdge);
                getContentView().addOnItemTouchListener(mSwipeItemTouchListener);
            }
        }
    }

    @Override
    public Object getValueBy(TangramExpr var) {
        if (var.hasNextFragment()) {
            String next = var.nextFragment();
            List<Card> cards = getGroupBasicAdapter().getGroups();
            for (int i = 0, size = cards.size(); i < size; i++) {
                Card card = cards.get(i);
                if (card.id.equals(next)) {
                    return card.getValueBy(var);
                }
            }
        }
        return null;
    }
}
