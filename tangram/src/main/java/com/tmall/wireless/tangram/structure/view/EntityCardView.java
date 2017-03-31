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

package com.tmall.wireless.tangram.structure.view;

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.wireless.tangram.core.adapter.BinderViewHolder;
import com.tmall.wireless.tangram.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.CellRender;
import com.tmall.wireless.tangram.structure.entitycard.EntityCard;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longerian on 16/8/19.
 */

public class EntityCardView extends ViewGroup implements ITangramViewLifeCycle {

    int mBgColor;

    float mAspectRatio = Float.NaN;

    int mItemCount = 0;

    EntityCard mEntityCard;

    private RecyclerView.RecycledViewPool mPool;

    private GroupBasicAdapter mAdapter;

    private List<BinderViewHolder> mViewHolders = new ArrayList<BinderViewHolder>();

    public EntityCardView(Context context) {
        this(context, null);
    }

    public EntityCardView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public EntityCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(null);
    }

    public void setBgColor(int bgColor) {
        this.mBgColor = bgColor;
    }

    public void setAspectRatio(float aspectRatio) {
        this.mAspectRatio = aspectRatio;
    }

    public int getItemCount() {
        return mItemCount;
    }

    public void setItemCount(int itemCount) {
        this.mItemCount = itemCount;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public void postBindView(BaseCell cell) {
        if (!mViewHolders.isEmpty()) {
            for (int i = 0, size = mViewHolders.size(); i < size; i++) {
                BinderViewHolder viewHolder = mViewHolders.get(i);
                viewHolder.unbind();
                removeView(viewHolder.itemView);
                mPool.putRecycledView(viewHolder);
            }
            mViewHolders.clear();
        }
        List<BaseCell> cells = mEntityCard.getCells();
        for (int i = 0, size = cells.size(); i < size; i++) {
            int itemViewType = mAdapter.getItemType(cells.get(i));
            BinderViewHolder holder = (BinderViewHolder) mPool.getRecycledView(itemViewType);
            if (holder == null) {
                holder = (BinderViewHolder) mAdapter.createViewHolder(this, itemViewType);
            }
            holder.bind(cells.get(i));
            addView(holder.itemView);
            mViewHolders.add(holder);
        }
    }

    @Override
    public void postUnBindView(BaseCell cell) {
        for (int i = 0, size = mViewHolders.size(); i < size; i++) {
            BinderViewHolder viewHolder = mViewHolders.get(i);
            viewHolder.unbind();
            removeView(viewHolder.itemView);
            mPool.putRecycledView(viewHolder);
        }
        mViewHolders.clear();
    }

    @Override
    public void cellInited(BaseCell cell) {
        this.mEntityCard = (EntityCard) cell;
        this.mPool = cell.serviceManager.getService(RecyclerView.RecycledViewPool.class);
        this.mAdapter = cell.serviceManager.getService(GroupBasicAdapter.class);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof VirtualLayoutManager.LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new VirtualLayoutManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new VirtualLayoutManager.LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new VirtualLayoutManager.LayoutParams(p);
    }

}
