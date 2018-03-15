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

package com.tmall.wireless.tangram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.Range;
import com.alibaba.android.vlayout.VirtualLayoutManager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import com.tmall.wireless.tangram.dataparser.DataParser;
import com.tmall.wireless.tangram.dataparser.IAdapterBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.ext.PullFromEndListener;
import com.tmall.wireless.tangram.ext.SwipeItemTouchListener;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.support.ExposureSupport;
import com.tmall.wireless.tangram.support.SimpleClickSupport;
import com.tmall.wireless.tangram.support.async.CardLoadSupport;
import com.tmall.wireless.tangram.util.Predicate;
import io.reactivex.functions.Consumer;
import org.json.JSONArray;

/**
 * Created by villadora on 15/8/24.
 *
 * The core of Tangram used to access data, bind view, register service.
 */
public class TangramEngine extends BaseTangramEngine<JSONArray, Card, BaseCell> implements Engine {

    private static final int NO_SWIPE = -1;

    public TangramEngine(@NonNull Context context,
        @NonNull DataParser<JSONArray, Card, BaseCell> dataParser,
        @NonNull IAdapterBuilder<Card, BaseCell> adapterBuilder) {
        super(context, dataParser, adapterBuilder);
        this.register(DataParser.class, dataParser);
    }

    private Runnable updateRunnable;

    private int mPreLoadNumber = 5;

    private boolean mEnableAutoLoadMore = true;

    private boolean mEnableLoadFirstPageCard = true;

    public int scrolledY;

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
        view.setOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView != null) {
                    scrolledY += dy;
                }
            }
        });
    }

    public void setPullFromEndListener(PullFromEndListener listener) {
        if (mSwipeItemTouchListener != null) {
            mSwipeItemTouchListener.setPullFromEndListener(listener);
        }
    }

    public void setNoScrolling(boolean noScrolling) {
        getLayoutManager().setNoScrolling(noScrolling);
    }

    public void setEnableOverlapMargin(boolean enable) {
        getLayoutManager().setEnableMarginOverlapping(enable);
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
                    loadSupport.reactiveDoLoadMore(current);
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
                    loadSupport.reactiveDoLoadMore(c);
                    loadedMore = true;
                } else {
                    loadSupport.doLoad(c);
                    loadSupport.reactiveDoLoad(c);
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
                    loadSupport.reactiveDoLoadMore(card);
                    loadedMore = true;
                } else {
                    loadSupport.doLoad(card);
                    loadSupport.reactiveDoLoad(card);
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
            loadSupport.reactiveDoLoadMore(groups.get(groups.size() - 1));
        }
    }

    /**
     * @param id Target card's id.
     * @return Return the target card.
     */
    public Card findCardById(String id) {
        MVHelper mvHelper = getService(MVHelper.class);
        if (mvHelper == null)
            return null;

        return mvHelper.resolver().findCardById(id);
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

                    if (mSwipeItemTouchListener != null) {
                        mSwipeItemTouchListener.updateCurrCard();
                    }
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

    /**
     * A high performance method to insert cells. TODO handle nested card
     * @param insertPosition the position to be inserted.
     * @param data new cell data
     * @since 2.1.0
     */
    public void insertWith(int insertPosition, BaseCell data) {
        insertWith(insertPosition, Arrays.asList(data));
    }


    /**
     * A high performance method to insert cells. Do not allowed to insert to an empty Tangram. TODO handle nested card
     * @param insertPosition the position to be inserted
     * @param list new cell data list
     * @since 2.1.0
     */
    public void insertWith(int insertPosition, List<BaseCell> list) {
        int newItemSize = list != null ? list.size() : 0;
        if (newItemSize > 0 && mGroupBasicAdapter != null) {
            if (insertPosition >= mGroupBasicAdapter.getItemCount()) {
                insertPosition = mGroupBasicAdapter.getItemCount() - 1;
            }
            BaseCell insertCell = mGroupBasicAdapter.getItemByPosition(insertPosition);
            int cardIdx = mGroupBasicAdapter.findCardIdxFor(insertPosition);
            Card card = mGroupBasicAdapter.getCardRange(cardIdx).second;
            card.addCells(card, card.getCells().indexOf(insertCell), list);
            List<LayoutHelper> layoutHelpers = getLayoutManager().getLayoutHelpers();
            if (layoutHelpers != null && cardIdx >= 0 && cardIdx < layoutHelpers.size()) {
                for (int i = 0, size = layoutHelpers.size(); i < size; i++) {
                    LayoutHelper layoutHelper = layoutHelpers.get(i);
                    int start = layoutHelper.getRange().getLower();
                    int end = layoutHelper.getRange().getUpper();
                    if (end < insertPosition) {
                        //do nothing
                    } else if (start <= insertPosition && insertPosition <= end) {
                        layoutHelper.setItemCount(layoutHelper.getItemCount() + newItemSize);
                        layoutHelper.setRange(start, end + newItemSize);
                    } else if (insertPosition < start) {
                        layoutHelper.setRange(start + newItemSize, end + newItemSize);
                    }
                }
                mGroupBasicAdapter.insertComponents(insertPosition, list);
            }
        }
    }

    /**
     * @param insertIdx the index to be inserted
     * @param group a group of data
     * @since 2.1.0
     */
    public void insertBatchWith(int insertIdx, Card group) {
        insertBatchWith(insertIdx, Arrays.asList(group));
    }

    /**
     * @param insertIdx the index to be inserted
     * @param groups a group list
     * @since 2.1.0
     */
    public void insertBatchWith(int insertIdx, List<Card> groups) {
        VirtualLayoutManager layoutManager = getLayoutManager();
        if (groups != null && groups.size() > 0 && mGroupBasicAdapter != null && layoutManager != null) {
            List<LayoutHelper> layoutHelpers = layoutManager.getLayoutHelpers();
            final List<LayoutHelper> newLayoutHelpers = new ArrayList<>(layoutHelpers);
            List<LayoutHelper> insertedLayoutHelpers = new ArrayList<>();
            for (int i = 0, size = groups.size(); i < size; i++) {
                insertedLayoutHelpers.add(groups.get(i).getLayoutHelper());
            }
            if (insertIdx >= layoutHelpers.size()) {
                newLayoutHelpers.addAll(insertedLayoutHelpers);
            } else {
                newLayoutHelpers.addAll(insertIdx, insertedLayoutHelpers);
            }
            layoutManager.setLayoutHelpers(newLayoutHelpers);
            mGroupBasicAdapter.insertBatchComponents(insertIdx, groups);
        }
    }

    /**
     * @param group new group to be append at tail.
     * @since 3.0.0
     */
    public void appendWith(Card group) {
        appendBatchWith(Arrays.asList(group));
    }

    /**
     * NOTE new API, use this to replace {@link BaseTangramEngine#appendData(List)} and {@link BaseTangramEngine#appendData(Object)}
     * @param groups new groups to be append at tail.
     * @since 2.1.0
     */
    public void appendBatchWith(List<Card> groups) {
        if (mGroupBasicAdapter != null) {
            insertBatchWith(mGroupBasicAdapter.getGroups().size(), groups);
        }
    }

    /**
     * Remove cell at target position. TODO handle nested card
     * @param position
     * @since 2.1.0
     */
    protected void removeBy(int position) {
        if (mGroupBasicAdapter != null) {
            if (position < mGroupBasicAdapter.getItemCount() && position >= 0) {
                BaseCell removeCell = mGroupBasicAdapter.getItemByPosition(position);
                removeBy(removeCell);
            }
        }
    }

    /**
     * Remove target cell. TODO handle nested card, cell in staggered, cell in onePlusN
     * @param data
     * @since 2.1.0
     */
    protected void removeBy(BaseCell data) {
        VirtualLayoutManager layoutManager = getLayoutManager();
        if (data != null && mGroupBasicAdapter != null && layoutManager != null) {
            int removePosition = mGroupBasicAdapter.getPositionByItem(data);
            if (removePosition >= 0) {
                int cardIdx = mGroupBasicAdapter.findCardIdxFor(removePosition);
                Card card = mGroupBasicAdapter.getCardRange(cardIdx).second;
                card.removeCellSilently(data);
                List<LayoutHelper> layoutHelpers = layoutManager.getLayoutHelpers();
                LayoutHelper emptyLayoutHelper = null;
                if (layoutHelpers != null && cardIdx >= 0 && cardIdx < layoutHelpers.size()) {
                    for (int i = 0, size = layoutHelpers.size(); i < size; i++) {
                        LayoutHelper layoutHelper = layoutHelpers.get(i);
                        int start = layoutHelper.getRange().getLower();
                        int end = layoutHelper.getRange().getUpper();
                        if (end < removePosition) {
                            //do nothing
                        } else if (start <= removePosition && removePosition <= end) {
                            int itemCount = layoutHelper.getItemCount() - 1;
                            if (itemCount > 0) {
                                layoutHelper.setItemCount(itemCount);
                                layoutHelper.setRange(start, end - 1);
                            } else {
                                emptyLayoutHelper = layoutHelper;
                            }
                        } else if (removePosition < start) {
                            layoutHelper.setRange(start - 1, end - 1);
                        }
                    }
                    if (emptyLayoutHelper != null) {
                        final List<LayoutHelper> newLayoutHelpers = new LinkedList<>(layoutHelpers);
                        newLayoutHelpers.remove(emptyLayoutHelper);
                        layoutManager.setLayoutHelpers(newLayoutHelpers);
                    }
                    mGroupBasicAdapter.removeComponent(data);
                }
            }
        }
    }

    /**
     * Remove all cells in a card with target index
     * @param removeIdx target card's index
     * @since 2.1.0
     */
    protected void removeBatchBy(int removeIdx) {
        if (mGroupBasicAdapter != null) {
            Pair<Range<Integer>, Card> cardPair = mGroupBasicAdapter.getCardRange(removeIdx);
            if (cardPair != null) {
                removeBatchBy(cardPair.second);
            }
        }
    }

    /**
     * Remove all cells in a card.
     * @param group
     * @since 2.1.0
     */
    protected void removeBatchBy(Card group) {
        VirtualLayoutManager layoutManager = getLayoutManager();
        if (group != null && mGroupBasicAdapter != null && layoutManager != null) {
            int cardIdx = mGroupBasicAdapter.findCardIdxForCard(group);
            List<LayoutHelper> layoutHelpers = layoutManager.getLayoutHelpers();
            LayoutHelper emptyLayoutHelper = null;
            int removeItemCount = 0;
            if (layoutHelpers != null && cardIdx >= 0 && cardIdx < layoutHelpers.size()) {
                for (int i = 0, size = layoutHelpers.size(); i < size; i++) {
                    LayoutHelper layoutHelper = layoutHelpers.get(i);
                    int start = layoutHelper.getRange().getLower();
                    int end = layoutHelper.getRange().getUpper();
                    if (i < cardIdx) {
                        // do nothing
                    } else if (i == cardIdx) {
                        removeItemCount = layoutHelper.getItemCount();
                        emptyLayoutHelper = layoutHelper;
                    } else {
                        layoutHelper.setRange(start - removeItemCount, end - removeItemCount);
                    }
                }
                if (emptyLayoutHelper != null) {
                    final List<LayoutHelper> newLayoutHelpers = new LinkedList<>(layoutHelpers);
                    newLayoutHelpers.remove(emptyLayoutHelper);
                    layoutManager.setLayoutHelpers(newLayoutHelpers);
                }
                mGroupBasicAdapter.removeComponents(group);
            }
        }
    }

    /**
     * Replace cell one by one.
     * @param oldOne
     * @param newOne
     * @since 2.1.0
     */
    public void replace(BaseCell oldOne, BaseCell newOne) {
        VirtualLayoutManager layoutManager = getLayoutManager();
        if (oldOne != null && newOne != null && mGroupBasicAdapter != null && layoutManager != null) {
            int replacePosition = mGroupBasicAdapter.getPositionByItem(oldOne);
            if (replacePosition >= 0) {
                int cardIdx = mGroupBasicAdapter.findCardIdxFor(replacePosition);
                Card card = mGroupBasicAdapter.getCardRange(cardIdx).second;
                card.replaceCell(oldOne, newOne);
                mGroupBasicAdapter.replaceComponent(Arrays.asList(oldOne), Arrays.asList(newOne));
            }
        }
    }

    /**
     * Replace parent card's children. Cells' size should be equal with parent's children size.
     * @param parent
     * @param cells
     * @since 2.1.0
     */
    public void replace(Card parent, List<BaseCell> cells) {
        VirtualLayoutManager layoutManager = getLayoutManager();
        if (parent != null && cells != null && cells.size() > 0 && mGroupBasicAdapter != null && layoutManager != null) {
            Card card = parent;
            List<BaseCell> oldChildren = new ArrayList<>(parent.getCells());
            if (oldChildren.size() == cells.size()) {
                card.setCells(cells);
                mGroupBasicAdapter.replaceComponent(oldChildren, cells);
            } else {
                List<LayoutHelper> layoutHelpers = layoutManager.getLayoutHelpers();
                int cardIdx = mGroupBasicAdapter.findCardIdxForCard(parent);
                int diff = 0;
                if (layoutHelpers != null && cardIdx >= 0 && cardIdx < layoutHelpers.size()) {
                    for (int i = 0, size = layoutHelpers.size(); i < size; i++) {
                        LayoutHelper layoutHelper = layoutHelpers.get(i);
                        int start = layoutHelper.getRange().getLower();
                        int end = layoutHelper.getRange().getUpper();
                        if (i < cardIdx) {
                            // do nothing
                        } else if (i == cardIdx) {
                            diff = cells.size() - layoutHelper.getItemCount();
                            layoutHelper.setItemCount(cells.size());
                            layoutHelper.setRange(start, end + diff);
                        } else {
                            layoutHelper.setRange(start + diff, end + diff);
                        }
                    }
                    card.setCells(cells);
                    mGroupBasicAdapter.replaceComponent(oldChildren, cells);
                }
            }
        }
    }

    /**
     * Replace card one by one. New one's children size should be equal with old one's children size.
     * @param oldOne
     * @param newOne
     * @since 2.1.0
     */
    public void replace(Card oldOne, Card newOne) {
        VirtualLayoutManager layoutManager = getLayoutManager();
        if (oldOne != null && newOne != null && mGroupBasicAdapter != null && layoutManager != null) {
            List<LayoutHelper> layoutHelpers = layoutManager.getLayoutHelpers();
            int cardIdx = mGroupBasicAdapter.findCardIdxForCard(oldOne);
            if (layoutHelpers != null && cardIdx >= 0 && cardIdx < layoutHelpers.size()) {
                final List<LayoutHelper> newLayoutHelpers = new LinkedList<>();
                for (int i = 0, size = layoutHelpers.size(); i < size; i++) {
                    LayoutHelper layoutHelper = layoutHelpers.get(i);
                    if (i == cardIdx) {
                        layoutHelper = newOne.getLayoutHelper();
                    }
                    newLayoutHelpers.add(layoutHelper);
                }
                layoutManager.setLayoutHelpers(newLayoutHelpers);
                mGroupBasicAdapter.replaceComponent(oldOne, newOne);
            }
        }
    }

    /**
     * Make engine as a consumer to accept data to append at the end of list
     * @return
     */
    public Consumer<List<Card>> asBatchAppendConsumer() {
        return new Consumer<List<Card>>() {
            @Override
            public void accept(List<Card> cards) throws Exception {
                appendBatchWith(cards);
            }
        };
    }

    /**
     * Make engine as a consumer to accept data to append at the end of list
     * @return
     */
    public Consumer<Card> asAppendConsumer() {
        return new Consumer<Card>() {
            @Override
            public void accept(Card card) throws Exception {
                appendWith(card);
            }
        };
    }

}