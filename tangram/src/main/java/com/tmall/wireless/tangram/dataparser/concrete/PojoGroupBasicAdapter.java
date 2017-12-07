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

package com.tmall.wireless.tangram.dataparser.concrete;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.Range;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.core.adapter.BinderViewHolder;
import com.tmall.wireless.tangram.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram.core.protocol.ControlBinder;
import com.tmall.wireless.tangram.structure.BaseCell;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import com.tmall.wireless.tangram.support.CellSupport;
import com.tmall.wireless.tangram.support.PageDetectorSupport;

import java.lang.annotation.Inherited;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adapter supports Card/Cell, it stores the <code>Data</code> for Tangram
 * Changes should work as:  change data in adapter -> do correct notify -> view get updated
 */
public class PojoGroupBasicAdapter extends GroupBasicAdapter<Card, BaseCell> {

    private int mLastBindPosition = -1;

    private AtomicInteger mTypeId = new AtomicInteger(0);

    private final Map<String, Integer> mStrKeys = new ArrayMap<>(64);

    private MVHelper mMvHelper;

    /*
     * This used to store cell.type <=> itemType mapping, they are not the same!
     * the same cell.type can be different itemTypes for better recycle mechanism
     */
    private final SparseArray<String> mId2Types = new SparseArray<>(64);

    PojoGroupBasicAdapter(@NonNull Context context, @NonNull VirtualLayoutManager layoutManager,
        @NonNull BaseCellBinderResolver componentBinderResolver,
        @NonNull BaseCardBinderResolver cardBinderResolver,
        @NonNull MVHelper mvHelper) {
        super(context, layoutManager, componentBinderResolver, cardBinderResolver);
        this.mMvHelper = mvHelper;
        setHasStableIds(true);
    }

    /**
     * Get correspond items for card
     *
     * @param card the card look items for
     * @return cells of this card
     */
    @Override
    public List<BaseCell> getItems(@NonNull Card card) {
        if (card.style != null && !TextUtils.isEmpty(card.style.forLabel)) {
            String forId = card.style.forLabel;
            if (mIdCardCache.containsKey(forId)) {
                Card forCard = mIdCardCache.get(forId);
                // if the "forLabel" card is empty, this card also should be empty
                if (forCard.mCells.size() == 0) {
                    if (TextUtils.isEmpty(forCard.load)) {
                        return null;
                    } else {
                        return Collections.emptyList();
                    }
                }
            }
        }

        if (TextUtils.isEmpty(card.load) && card.mCells.isEmpty()) {
            return null;
        }
        return new LinkedList<>(card.mCells);
    }

    @Override
    public <V extends View> BinderViewHolder<BaseCell, V> createViewHolder(@NonNull ControlBinder<BaseCell, V> binder, @NonNull Context context, ViewGroup parent) {
        V view = binder.createView(context, parent);
        return new BinderViewHolder<>(view, binder);
    }


    @Override
    public void onBindViewHolder(BinderViewHolder<BaseCell, ? extends View> holder, int position) {
        super.onBindViewHolder(holder, position);
        int idx = findCardIdxFor(position);

        if (idx >= 0) {
            Pair<Range<Integer>, Card> pair = mCards.get(idx);
            pair.second.onBindCell(position - pair.first.getLower(), position, mLastBindPosition < 0 || mLastBindPosition < position);
            PageDetectorSupport pageDetectorSupport = pair.second.serviceManager
                .getService(PageDetectorSupport.class);
            if (pageDetectorSupport != null) {
                pageDetectorSupport.onBindItem(position, mLastBindPosition < 0 || mLastBindPosition < position, getItemByPosition(position));
            }
        }

        mLastBindPosition = position;
    }

    /**
     * Get type of card
     *
     * @param card
     * @return the type of card
     */
    @Override
    public int getCardType(Card card) {
        return card.type;
    }

    @Override
    public String getCardStringType(Card card) {
        return card.stringType;
    }

    @Override
    public String getCellTypeFromItemType(int viewType) {
        if (mId2Types.indexOfKey(viewType) < 0) {
            throw new IllegalStateException("Can not found item.type for viewType: " + viewType);
        }
        return mId2Types.get(viewType);
    }

    /**
     * Find itemType for Cell
     *
     * @param item the cell data
     * @return itemType as int used for recycled id
     */
    @Override
    public int getItemType(BaseCell item) {
        // if the item is a keyType, which means item.type is not the key
        if (!TextUtils.isEmpty(item.typeKey)) {
            // we should use getTypeKey()
            String typeKey = item.typeKey;
            if (!mStrKeys.containsKey(typeKey)) {
                int newType = mTypeId.getAndIncrement();
                mStrKeys.put(typeKey, newType);
                mId2Types.put(newType, item.stringType);
            }

            return mStrKeys.get(typeKey).intValue();
        } else {
            // otherwise, use item.type as identity key
            // note, this now only be executed in MainThread, atomic not actually needed
            String stringType = item.stringType;
            if (!mStrKeys.containsKey(stringType)) {
                int newType = mTypeId.getAndIncrement();
                mStrKeys.put(item.stringType, newType);
                mId2Types.put(newType, item.stringType);
            }
            return mStrKeys.get(item.stringType).intValue();
        }
    }


    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).objectId;
    }

    @Override
    public void appendGroup(@Nullable List<Card> cards) {
        super.appendGroup(cards);
    }


    @Override
    protected void diffGroup(SparseArray<Card> added, SparseArray<Card> removed) {
        for (int i = 0, size = removed.size(); i < size; i++) {
            int key = removed.keyAt(i);
            Card card = removed.get(key);
            if (card != null) {
                try {
                    card.removed();
                } catch (Exception e) {
                    if (card.serviceManager != null) {
                        CellSupport cellSupport = card.serviceManager.getService(CellSupport.class);
                        if (card.extras != null) {
                            cellSupport.onException(card.extras.toString(), e);
                        } else {
                            cellSupport.onException(card.stringType, e);
                        }
                    }
                }
            }
        }

        for (int i = 0, size = added.size(); i < size; i++) {
            int key = added.keyAt(i);
            Card card = added.get(key);
            if (card != null) {
                try {
                    card.added();
                } catch (Exception e) {
                    if (card.serviceManager != null) {
                        CellSupport cellSupport = card.serviceManager.getService(CellSupport.class);
                        if (card.extras != null) {
                            cellSupport.onException(card.extras.toString(), e);
                        } else {
                            cellSupport.onException(card.stringType, e);
                        }
                    }
                }
            }
        }
    }


    private final Map<String, Card> mIdCardCache = new ArrayMap<>(64);

    @NonNull
    @Override
    protected List<LayoutHelper> transformCards(@Nullable List<Card> cards, @NonNull List<BaseCell> data, @NonNull List<Pair<Range<Integer>, Card>> rangeCards) {
        if (cards == null) {
            return super.transformCards(cards, data, rangeCards);
        }

        /* record card id, used in {@link #getItems} to clean "forLabel" cards */
        for (Card card : cards) {
            if (!TextUtils.isEmpty(card.id)) {
                mIdCardCache.put(card.id, card);
            }
        }

        List<LayoutHelper> layoutHelpers = super.transformCards(cards, data, rangeCards);

        // clean cache after used
        mIdCardCache.clear();

        return layoutHelpers;
    }

    public int findFirstPositionOfCell(int type) {
        List<BaseCell> data = getComponents();
        int targetPosition = -1;
        if (data == null || data.isEmpty()) {
            targetPosition = -1;
        } else {
            for (int i = 0, size = data.size(); i < size; i++) {
                if (String.valueOf(type).equals(data.get(i).stringType)) {
                    targetPosition = i;
                    break;
                }
            }
        }
        return targetPosition;
    }

    public int findLastPositionOfCell(int type) {
        List<BaseCell> data = getComponents();
        int targetPosition = -1;
        if (data == null || data.isEmpty()) {
            targetPosition = -1;
        } else {
            for (int i = data.size() - 1; i >= 0; i--) {
                if (String.valueOf(type).equals(data.get(i).stringType)) {
                    targetPosition = i;
                    break;
                }
            }
        }
        return targetPosition;
    }

    public Range<Integer> getCardRange(String id) {
        if (TextUtils.isEmpty(id)) {
            return Range.create(0, 0);
        }

        List<Card> cards = getGroups();
        for (int i = 0, size = cards.size(); i < size; i++) {
            Card c = cards.get(i);
            if (id.equals(c.id)) {
                return getCardRange(c);
            }
        }
        return Range.create(0, 0);
    }


    /**
     * remove a component
     *
     * @param position the position to be removes
     */
    @Override
    public void removeComponent(int position) {
        if (mData != null) {
            if (position >= 0 && position <= mData.size() - 1) {
                BaseCell cell = mData.remove(position);
                boolean changed = cell != null;
                int idx = findCardIdxFor(position);
                if (idx >= 0) {
                    Pair<Range<Integer>, Card> pair = mCards.get(idx);
                    pair.second.removeCellSilently(cell);
                }
                if (changed) {
                    notifyItemRemoved(position);
                    int last = mLayoutManager.findLastVisibleItemPosition();
                    notifyItemRangeChanged(position, last - position);
                }
            }
        }
    }

    /**
     * @param component the component to be removed
     */
    @Override
    public void removeComponent(BaseCell component) {
        if (mData != null && component != null) {
            int position = mData.indexOf(component);
            if (position >= 0) {
                mData.remove(component);
                int idx = findCardIdxFor(position);
                if (idx >= 0) {
                    Pair<Range<Integer>, Card> pair = mCards.get(idx);
                    pair.second.removeCellSilently(component);
                }
                notifyItemRemoved(position);
                int last = mLayoutManager.findLastVisibleItemPosition();
                notifyItemRangeChanged(position, last - position);
            }
        }
    }

    @Override
    public void insertComponents(int pos, List<BaseCell> components) {
        if (mData != null && components != null && !components.isEmpty() && pos > 0) {
            for (int i = 0; i < components.size(); i++) {
                if ((pos + i) < mData.size()) {
                    mData.add(pos + i, components.get(i));
                }
            }
        }
        notifyItemRangeInserted(pos, components.size());
    }

    @Override
    public void destroy() {
        super.destroy();
        for (int i = 0, size = mCards.size(); i < size; i++) {
            mCards.get(i).second.removed();
        }
    }
}

