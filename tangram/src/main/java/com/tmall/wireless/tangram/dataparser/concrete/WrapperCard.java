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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.card.GridCard;

import java.util.ArrayList;
import java.util.List;

/**
 * A card which can be used as a extensible wrapper card for existing card
 */
public class WrapperCard extends Card {

    @NonNull
    private Card mCard;

    public WrapperCard(@NonNull Card card) {
        this.mCard = card;
        List<BaseCell> cells = new ArrayList<>(mCard.getCells());
        this.mCard.setCells(null);

        this.type = mCard.type;
        this.id = mCard.id;
        this.loaded = mCard.loaded;
        this.load = mCard.load;
        this.loading = mCard.loading;
        this.loadMore = mCard.loadMore;
        this.hasMore = mCard.hasMore;
        this.page = mCard.page;
        this.style = mCard.style;
        this.maxChildren = mCard.maxChildren;
        this.rowId = mCard.rowId;
        this.serviceManager = mCard.serviceManager;
        this.setParams(mCard.getParams());

        this.setCells(cells);
        this.addCells(mCard.mPendingCells);
    }

    @Override
    public boolean isValid() {
        return mCard.isValid();
    }

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper oldHelper) {
        LayoutHelper layoutHelper = mCard.convertLayoutHelper(oldHelper);
        if (layoutHelper != null) {
            layoutHelper.setItemCount(mCells.size());
            if (layoutHelper instanceof GridLayoutHelper) {
                GridLayoutHelper gridHelper = (GridLayoutHelper) layoutHelper;
                gridHelper.setSpanSizeLookup(new GridCard.CellSpanSizeLookup(mCells, gridHelper.getSpanCount()));
            }
        }

        return layoutHelper;
    }
}
