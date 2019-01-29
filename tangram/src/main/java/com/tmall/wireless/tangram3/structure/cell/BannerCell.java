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

package com.tmall.wireless.tangram3.structure.cell;

import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import com.alibaba.android.vlayout.RecyclablePagerAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager.LayoutParams;
import com.alibaba.fastjson.JSONObject;
import com.tmall.ultraviewpager.UltraViewPagerAdapter;
import com.tmall.wireless.tangram3.core.adapter.BinderViewHolder;
import com.tmall.wireless.tangram3.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram3.structure.BaseCell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mikeafc on 16/1/13.
 */
public class BannerCell extends BaseCell {

    public int mIndicatorRadius;
    public int mIndicatorColor;
    public int mIndicatorDefaultColor;

    public int mAutoScrollInternal;
    public SparseIntArray mSpecialInterval;
    public boolean mInfinite;
    public int mInfiniteMinCount;
    public String mIndicatorFocus;
    public String mIndicatorNor;
    public String mIndicatorGravity;
    public String mIndicatorPos;
    public int mIndicatorGap;
    public int mIndicatorMargin;
    public int mIndicatorHeight;
    public float mRatio;
    public int mBgColor;
    public float pageWidth = Float.NaN;
    public int hGap;
    public int[] itemMargin = new int[2];
    public int[] margin = new int[4];
    public int height = LayoutParams.WRAP_CONTENT;
    public double itemRatio;

    public UltraViewPagerAdapter mBannerWrapper;
    private BannerAdapter mBannerAdapter;
    public List<BaseCell> mCells = new ArrayList<>();
    public BaseCell mHeader;
    public BaseCell mFooter;

    public void setData(List<BaseCell> cells) {
        initAdapter();
        this.mCells.clear();
        this.mCells.addAll(cells);
        mBannerAdapter.notifyDataSetChanged();
    }

    public void initAdapter() {
        if (mBannerAdapter == null) {
            if (serviceManager != null) {
                GroupBasicAdapter adapter = serviceManager.getService(GroupBasicAdapter.class);
                RecyclerView.RecycledViewPool pool = serviceManager.getService(RecyclerView.RecycledViewPool.class);
                mBannerAdapter = new BannerAdapter(adapter, pool);
            }
        }
        if (mBannerWrapper == null) {
            mBannerWrapper = new UltraViewPagerAdapter(mBannerAdapter);
        }
    }

    class BannerAdapter extends RecyclablePagerAdapter<BinderViewHolder> {

        private GroupBasicAdapter adapter;

        public BannerAdapter(GroupBasicAdapter adapter, RecyclerView.RecycledViewPool pool) {
            super(adapter, pool);
            this.adapter = adapter;
        }

        @Override
        public int getCount() {
            return mCells.size();
        }

        @Override
        public void onBindViewHolder(BinderViewHolder viewHolder, int position) {
            viewHolder.bind(mCells.get(position));
        }

        @Override
        public int getItemViewType(int position) {
            return adapter.getItemType(mCells.get(position));
        }

        @Override
        public float getPageWidth(int position) {
            return Float.isNaN(pageWidth) ? 1f : pageWidth;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof BinderViewHolder) {
                ((BinderViewHolder) object).unbind();
            }
            super.destroyItem(container, position, object);
        }
    }

    public void setAutoScrollInternal(int mAutoScroll_internal) {
        this.mAutoScrollInternal = mAutoScroll_internal;
    }

    public void setSpecialInterval(JSONObject jsonObject) {
        if (jsonObject != null) {
            this.mSpecialInterval = new SparseIntArray();
            for (String key : jsonObject.keySet()) {
                try {
                    int index = Integer.parseInt(key);
                    int value = jsonObject.getIntValue(key);
                    if (value > 0) {
                        this.mSpecialInterval.put(index, value);
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    public void setInfinite(boolean mInfinite) {
        this.mInfinite = mInfinite;
    }

    public void setInfiniteMinCount(int count) {
        mInfiniteMinCount = count;
    }

    public void setIndicatorFocus(String mIndicator_focus) {
        this.mIndicatorFocus = mIndicator_focus;
    }

    public void setIndicatorNor(String mIndicator_nor) {
        this.mIndicatorNor = mIndicator_nor;
    }

    public void setIndicatorGravity(String mIndicator_gravity) {
        this.mIndicatorGravity = mIndicator_gravity;
    }

    public void setIndicatorPos(String mIndicator_pos) {
        this.mIndicatorPos = mIndicator_pos;
    }

    public void setIndicatorGap(int mIndicator_gap) {
        this.mIndicatorGap = mIndicator_gap;
    }

    public void setIndicatorMargin(int mIndicator_margin) {
        this.mIndicatorMargin = mIndicator_margin;
    }

    public void setRatio(float ratio) {
        this.mRatio = ratio;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setBgColor(int bgColor) {
        this.mBgColor = bgColor;
    }

    public void setPageWidth(double pageWidth) {
        this.pageWidth = (float) pageWidth;
    }

    public void sethGap(int hGap) {
        this.hGap = hGap;
    }

    public void setIndicatorHeight(int indicatorHeight) {
        mIndicatorHeight = indicatorHeight;
    }

    public void setIndicatorRadius(int indicatorRadius) {
        mIndicatorRadius = indicatorRadius;
    }

    public void setIndicatorColor(int indicatorColor) {
        mIndicatorColor = indicatorColor;
    }

    public void setIndicatorDefaultColor(int indicatorDefaultColor) {
        mIndicatorDefaultColor = indicatorDefaultColor;
    }
}
