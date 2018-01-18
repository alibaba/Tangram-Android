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

package com.tmall.wireless.tangram.core.adapter;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.Range;
import com.alibaba.android.vlayout.VirtualLayoutAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.wireless.tangram.core.protocol.ControlBinder;
import com.tmall.wireless.tangram.core.protocol.ControlBinderResolver;
import com.tmall.wireless.tangram.core.protocol.LayoutBinder;
import com.tmall.wireless.tangram.core.protocol.LayoutBinderResolver;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.util.Preconditions;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by villadora on 15/8/19.
 */
public abstract class GroupBasicAdapter<L, C> extends VirtualLayoutAdapter<BinderViewHolder<C, ? extends View>> {


    @NonNull
    private final Context mContext;


    @NonNull
    protected ArrayList<Pair<Range<Integer>, L>> mCards = new ArrayList<>();

    @NonNull
    protected List<C> mData = new LinkedList<>();

    private ControlBinderResolver<? extends ControlBinder<C, ? extends View>> mCompBinderResolver;

    private LayoutBinderResolver<L, ? extends LayoutBinder<L>> mCardBinderResolver;

    public GroupBasicAdapter(@NonNull final Context context, @NonNull final VirtualLayoutManager layoutManager,
        @NonNull ControlBinderResolver<? extends ControlBinder<C, ? extends View>> cellBinderResolver,
        @NonNull LayoutBinderResolver<L, ? extends LayoutBinder<L>> cardBinderResolver) {
        super(layoutManager);

        mContext = Preconditions.checkNotNull(context, "context should not be null");

        mCompBinderResolver = Preconditions.checkNotNull(cellBinderResolver, "componentBinderResolver should not be null");
        mCardBinderResolver = Preconditions.checkNotNull(cardBinderResolver, "layoutBinderResolver should not be null");
    }

    private final SparseBooleanArray pendingDeleteMap = new SparseBooleanArray();
    private final SparseArray<L> oldMap = new SparseArray<>(64);
    private final SparseArray<L> newMap = new SparseArray<>(64);

    private void createSnapshot() {
        oldMap.clear();
        List<L> groups = getGroups();
        for (int i = 0, size = groups.size(); i < size; i++) {
            L group = groups.get(i);
            oldMap.put(System.identityHashCode(group), group);
        }
    }


    private void diffWithSnapshot() {
        pendingDeleteMap.clear();
        newMap.clear();

        List<L> groups = getGroups();
        for (int i = 0, size = groups.size(); i < size; i++) {
            L group = groups.get(i);
            newMap.put(System.identityHashCode(group), group);
        }

        for (int i = 0, size = newMap.size(); i < size; i++) {
            int key = newMap.keyAt(i);
            if (oldMap.get(key) != null) {
                oldMap.remove(key);
                pendingDeleteMap.put(key, true);
            }
        }
        for (int i = 0, size = pendingDeleteMap.size(); i < size; i ++) {
            newMap.remove(pendingDeleteMap.keyAt(i));
        }

        diffGroup(newMap, oldMap);

        oldMap.clear();
        newMap.clear();
    }

    /**
     * set data without call {@link RecyclerView.Adapter#notifyDataSetChanged()}
     * @param cards new cards data
     */
    public void setData(@Nullable List<L> cards) {
        setData(cards, false);
    }

    /**
     * @param cards new cards data
     * @param silence true, call {@link RecyclerView.Adapter#notifyDataSetChanged()}; false do not call{@link RecyclerView.Adapter#notifyDataSetChanged()}
     */
    public void setData(@Nullable List<L> cards, boolean silence) {
        createSnapshot();

        mCards.clear();
        mData.clear();


        if (cards != null && cards.size() != 0) {
            mCards.ensureCapacity(cards.size());
            setLayoutHelpers(transformCards(cards, mData, mCards));
        } else {
            setLayoutHelpers(Collections.<LayoutHelper>emptyList());
        }

        diffWithSnapshot();

        if (!silence)
            notifyDataSetChanged();
    }

    /**
     * append new cards into adapter
     *
     * @param cards new cards will be added to the end
     */
    public void appendGroup(@Nullable List<L> cards) {
        if (cards == null || cards.size() == 0)
            return;

        createSnapshot();

        final List<LayoutHelper> helpers = new LinkedList<>(getLayoutHelpers());

        mCards.ensureCapacity(mCards.size() + cards.size());

        helpers.addAll(transformCards(cards, mData, mCards));

        setLayoutHelpers(helpers);

        diffWithSnapshot();

        // not use {@link notifyItemRangeChanged} because not sure whether animations is required
        // notifyItemRangeChanged(oldItem, newitems);
        notifyDataSetChanged();
    }

    /**
     * Get correspond items for card
     *
     * @param card the card look items for
     * @return cells of this card
     */
    protected abstract List<C> getItems(@NonNull L card);

    /**
     * @param position adapter position
     * @return the cell instance at given adapter position
     */
    public C getItemByPosition(int position) {
        return mData.get(position);
    }

    /**
     * @param c cell instance
     * @return the cell's adapter position of given instance
     */
    public int getPositionByItem(C c) {
        return mData.indexOf(c);
    }

    /**
     * Transform cards to layoutHelpers with correct range and add cells in cards into data
     *
     * @param cards cards will be transformed
     * @param data  list of items that items will be added to
     * @return helpers transformed from cards
     */
    @NonNull
    protected List<LayoutHelper> transformCards(@Nullable List<L> cards, final @NonNull List<C> data,
        @NonNull List<Pair<Range<Integer>, L>> rangeCards) {
        if (cards == null || cards.size() == 0) {
            return new LinkedList<>();
        }


        int lastPos = data.size();
        List<LayoutHelper> helpers = new ArrayList<>(cards.size());
        for (int i = 0, size = cards.size(); i < size; i++) {
            L card = cards.get(i);

            if (card == null) continue;

            final String ctype = getCardStringType(card);
            List<C> items = getItems(card);
            if (items == null) {
                // skip card null
                continue;
            }

            data.addAll(items);

            // calculate offset to set ranges
            int offset = lastPos;
            lastPos += items.size();
            // include [x, x) for empty range, upper are not included in range
            rangeCards.add(Pair.create(Range.create(offset, lastPos), card));

            // get layoutHelper for this card
            LayoutBinder<L> binder = mCardBinderResolver.create(ctype);
            LayoutHelper helper = binder.getHelper(ctype, card);


            if (helper != null) {
                helper.setItemCount(items.size());
                helpers.add(helper);
            }
        }

        return helpers;
    }


    @Override
    public BinderViewHolder<C, ? extends View> onCreateViewHolder(ViewGroup parent, int viewType) {
        String cellType = getCellTypeFromItemType(viewType);
        ControlBinder<C, ? extends View> binder = mCompBinderResolver.create(cellType);
        return createViewHolder(binder, mContext, parent);
    }


    public abstract <V extends View> BinderViewHolder<C, V> createViewHolder(
        @NonNull final ControlBinder<C, V> binder, @NonNull final Context context, final ViewGroup parent);


    @Override
    public void onBindViewHolder(BinderViewHolder<C, ? extends View> holder, int position) {
        // position must be valid
        C data = mData.get(position);
        holder.bind(data);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewRecycled(BinderViewHolder<C, ? extends View> holder) {
        holder.unbind();
    }


    /**
     * should be called when page destroy by user
     */
    public void destroy() {
    }

    /**
     * @param index card index
     * @return card range at given index
     */
    public Pair<Range<Integer>, L> getCardRange(int index) {
        if (index >= 0 && index <= mCards.size() - 1) {
            return mCards.get(index);
        } else {
            return null;
        }
    }

    /**
     *
     * @param card
     * @return card range of given instance
     */
    public Range<Integer> getCardRange(Card card) {
        if (card == null) return Range.create(0, 1);

        int idx = getGroups().indexOf(card);
        if (idx >= 0) {
            return mCards.get(idx).first;
        } else {
            return Range.create(0, 1);
        }
    }

    /**
     *
     * @param card
     * @return card index of given instance
     */
    public int findCardIdxForCard(L card) {
        for (int i = 0, size = mCards.size(); i < size; i++) {
            if (mCards.get(i).second == card) {
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * @param position cell's adapter position
     * @return the card index of given cell's position
     */
    public int findCardIdxFor(int position) {
        int low = 0, mid = mCards.size(), high = mid - 1;
        while (low <= high) {
            mid = (low + high) >>> 1;
            Pair<Range<Integer>, L> pair = mCards.get(mid);

            if (pair == null) return -1;

            if (pair.first.getLower() <= position && pair.first.getUpper() > position) {
                return mid;
            } else if (pair.first.getUpper() <= position) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return -1;
    }

    /**
     *
     * @param cell cell object
     * @return the card index of given cell object
     */
    public int findCardIdxFor(C cell) {
        int position = mData.indexOf(cell);
        return findCardIdxFor(position);
    }


    /**
     * Get type of card
     *
     * @param card
     * @return the type of card
     */
	@Deprecated
    public abstract int getCardType(L card);

    /**
     * Get card range by id
     * @param id card id
     * @return range instance
     */
    public abstract Range<Integer> getCardRange(String id);

    /**
     *
     * @param id card id
     * @return card instance
     */
    public abstract Card getCardById(String id);

    /**
     *
     * @param type cell's type
     * @return last appearance position
     */
    public abstract int findLastPositionOfCell(String type);

    /**
     *
     * @param type cell's type
     * @return first appearance position
     */
    public abstract int findFirstPositionOfCell(String type);

    /**
     * Get type of card in String type
     *
     * @param card
     * @return the type of card
     */
    public abstract String getCardStringType(L card);

    /**
     * Get Tangram cell type from recyclerView's item type
     * @param viewType
     * @return cell type in Tangram
     */
    public abstract String getCellTypeFromItemType(int viewType);

    /**
     * Find itemType for Cell
     *
     * @param item the cell data
     * @return itemType as int used for recycled id
     */
    public abstract int getItemType(C item);

    @Override
    public int getItemViewType(int position) {
        C data = mData.get(position);
        return getItemType(data);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }


    public void notifyUpdate() {
        notifyUpdate(true);
    }

    public void notifyUpdate(boolean layoutUpdated) {
        if (layoutUpdated) {
            setData(getGroups());
        } else {
            notifyDataSetChanged();
        }
    }

    /**
     * Refresh inner data structure without notifyDataSetChange, must be followed with a notifyDataSetChanged or other notify methods
     */
    public void refreshWithoutNotify() {
        setData(getGroups(), true);
    }


    /**
     * replace group in position replaced with new groups
     *
     * @param replaced index that will be replaced
     * @param groups   new groups
     */
    public void replaceGroup(int replaced, @Nullable List<L> groups) {

        if (replaced < 0 || replaced >= mCards.size() || groups == null || groups.size() == 0)
            // invalid index
            return;

        List<L> cards = getGroups();

        boolean changed = cards.addAll(replaced + 1, groups);

        if (changed) {
            cards.remove(replaced);
            setData(cards);
        }
    }

    /**
     * insert groups after position inserted
     *
     * @param inserted index that will inserted after
     * @param groups   new groups
     */
    public void insertGroup(int inserted, @Nullable List<L> groups) {
        if (inserted < 0 || inserted >= mCards.size() || groups == null || groups.size() == 0)
            return;

        List<L> cards = getGroups();

        boolean changed = cards.addAll(inserted, groups);

        if (changed)
            setData(cards);
    }

    /**
     * remove a group
     *
     * @param group the group to be removed
     */
    public void removeGroup(@Nullable L group) {
        if (group == null) {
            return;
        }
        List<L> cards = getGroups();
        boolean changed = cards.remove(group);
        if (changed) {
            setData(cards);
        }
    }

    /**
     * remove a group
     *
     * @param removed the group index to be removed
     */
    public void removeGroup(int removed) {
        List<L> cards = getGroups();
        if (removed >= 0 && removed < cards.size()) {
            boolean changed = cards.remove(removed) != null;
            if (changed) {
                setData(cards);
            }
        }
    }

    /**
     * !!! Do not call this method directly. It's not designed for users.
     * @param position the position to be removed
     */
    abstract public void removeComponent(int position);

    /**
     * !!! Do not call this method directly. It's not designed for users.
     * @param component the component to be removed
     */
    abstract public void removeComponent(C component);

    /**
     *
     * @param group a group of components to be removed.
     */
    abstract public void removeComponents(L group);

    /**
     * Do not call this method directly. It's not designed for users.
     * @param pos
     * @param components
     */
    abstract public void insertComponents(int pos, List<C> components);

    /**
     * @return total card list
     */
    public List<L> getGroups() {
        List<L> cards = new ArrayList<>(mCards.size());
        for (int i = 0, size = mCards.size(); i < size; i++) {
            Pair<Range<Integer>, L> pair = mCards.get(i);
            cards.add(pair.second);
        }
        return cards;
    }

    /**
     *
     * @return total cell list
     */
    public List<C> getComponents() {
        return new ArrayList<>(mData);
    }


    protected void diffGroup(SparseArray<L> added, SparseArray<L> removed) {

    }

}
