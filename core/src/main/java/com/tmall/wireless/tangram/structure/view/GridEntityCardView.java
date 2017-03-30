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
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.card.GridCard;
import com.tmall.wireless.tangram.structure.entitycard.EntityCard;
import com.tmall.wireless.tangram.structure.entitycard.GridEntityCard;
import com.tmall.wireless.tangram.support.CardSupport;
import com.tmall.wireless.tangram.util.ImageUtils;
import com.tmall.wireless.tangram.util.Utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Arrays;

/**
 * Created by longerian on 16/8/20.
 */

public class GridEntityCardView extends EntityCardView implements ITangramViewLifeCycle {

    private ImageView bgImageView;

    private static final String TAG = "GridEntityCardView";

    private int column;

    private int mSpanCount = 4;

    private int mTotalSize = 0;

    private boolean mIsAutoExpand = true;

    private boolean mIgnoreExtra = false;

    private int mVGap = 0;
    private int mHGap = 0;

    private float[] mWeights;

    private int[] mSizePerSpan;

    private GridLayoutHelper.SpanSizeLookup mSpanSizeLookup = new DefaultSpanSizeLookup();

    public GridEntityCardView(Context context) {
        this(context, null);
    }

    public GridEntityCardView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public GridEntityCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClipToPadding(false);
        setClipChildren(false);
    }

    public void setWeights(float[] weights) {
        if (weights != null) {
            this.mWeights = Arrays.copyOf(weights, weights.length);
        } else {
            this.mWeights = new float[0];
        }
    }


    public void setSpanSizeLookup(GridLayoutHelper.SpanSizeLookup spanSizeLookup) {
        if (spanSizeLookup != null) {
            this.mSpanSizeLookup = spanSizeLookup;
            this.mSpanSizeLookup.setStartPosition(0);
            this.mSpanSizeLookup.setSpanIndexCacheEnabled(true);
        }
    }

    public void setAutoExpand(boolean isAutoExpand) {
        this.mIsAutoExpand = isAutoExpand;
    }

    public void setIgnoreExtra(boolean ignoreExtra) {
        this.mIgnoreExtra = ignoreExtra;
    }


    /**
     * {@inheritDoc}
     * Set SpanCount for grid
     *
     * @param spanCount grid column number, must be greater than 0. {@link IllegalArgumentException}
     *                  will be thrown otherwise
     */
    public void setSpanCount(int spanCount) {
        if (spanCount == mSpanCount) {
            return;
        }
        if (spanCount < 1) {
            throw new IllegalArgumentException("Span count should be at least 1. Provided "
                    + spanCount);
        }
        mSpanCount = spanCount;
    }

    public int getSpanCount() {
        return mSpanCount;
    }

    public void setGap(int gap) {
        setVGap(gap);
        setHGap(gap);
    }

    public void setVGap(int vGap) {
        if (vGap < 0) vGap = 0;
        this.mVGap = vGap;
    }

    public void setHGap(int hGap) {
        if (hGap < 0) hGap = 0;
        this.mHGap = hGap;
    }

    private int getMainDirSpec(int dim, int otherSize) {
        if (!Float.isNaN(mAspectRatio) && mAspectRatio > 0) {
            return View.MeasureSpec.makeMeasureSpec((int) (otherSize / mAspectRatio), View.MeasureSpec.EXACTLY);
        } else if (dim < 0) {
            return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        } else {
            return View.MeasureSpec.makeMeasureSpec(dim, View.MeasureSpec.EXACTLY);
        }
    }

    private void calculateSizePerSpan(int totalSpace) {
        int available = totalSpace - (mSpanCount - 1) * mHGap;
        if (mSizePerSpan == null || mSizePerSpan.length != mSpanCount) {
            mSizePerSpan = new int[mSpanCount];
        }
        if (mWeights != null && mWeights.length > 0) {
            int eqCnt = 0, remainingSpace = available;
            int colCnt = mSpanCount;
            for (int i = 0; i < colCnt; i++) {
                if (i < mWeights.length && !Float.isNaN(mWeights[i]) && mWeights[i] >= 0) {
                    float weight = mWeights[i];
                    mSizePerSpan[i] = (int) (weight * 1.0f / 100 * available + 0.5f);
                    remainingSpace -= mSizePerSpan[i];
                } else {
                    eqCnt++;
                    mSizePerSpan[i] = -1;
                }
            }
            if (eqCnt > 0) {
                int eqLength = (int) (remainingSpace * 1.0f / eqCnt + 0.5f);
                for (int i = 0; i < colCnt; i++) {
                    if (mSizePerSpan[i] < 0) {
                        mSizePerSpan[i] = eqLength;
                    }
                }
            }

        } else {
            for (int i = 0; i < mSpanCount; i++) {
                mSizePerSpan[i] = (int) (available * 1.0f / mSpanCount + 0.5f);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "width spec " + MeasureSpec.toString(widthMeasureSpec));
        Log.i(TAG, "height spec " + MeasureSpec.toString(heightMeasureSpec));
        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        mTotalSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight() - getPaddingLeft()
                - (lp != null ? (lp.leftMargin + lp.rightMargin) : 0);
        calculateSizePerSpan(mTotalSize);

        int totalHeight = 0;
        int childWidth = 0;
        int childHeight = 0;
        int lastSpanGroupIndex = 0;
        int childOffset = bgImageView != null && indexOfChild(bgImageView) == 0 ? 1 : 0;
        for (int i = 0, count = getChildCount(); i < count - childOffset; i++) {
            int cellIndex = i + childOffset;
            int spanGroupIndex = mSpanSizeLookup.getSpanGroupIndex(i, mSpanCount);
            if (spanGroupIndex != lastSpanGroupIndex) {
                lastSpanGroupIndex = spanGroupIndex;
                childHeight = 0;
            }
            childWidth = 0;
            View child = getChildAt(cellIndex);
            VirtualLayoutManager.LayoutParams childLp = (VirtualLayoutManager.LayoutParams) child.getLayoutParams();
            int spanSize = mSpanSizeLookup.getSpanSize(i);
            int spanIdex = mSpanSizeLookup.getSpanIndex(i, mSpanCount);
            for (int j = 0; j < spanSize; j++) {
                childWidth += mSizePerSpan[j + spanIdex];
            }
            childWidth = childWidth + Math.max(0, spanSize - 1) * mHGap;
            //childHeight == 0 表示一行起始item,高度有wrapcontent
            //childHeight > 0 表示非第一个 item,和第一个对齐,保持一行内 item 高度一致
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                    childHeight > 0 ? MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
                            : getMainDirSpec(-1, mTotalSize));
            Log.i(TAG,
                    "child " + i + " spanSize " + spanSize + " viewSize " + childWidth + " " + child
                            .getMeasuredWidth() + " " + child.getMeasuredHeight());
            if (spanIdex == 0) {
                childHeight = child.getMeasuredHeight() + childLp.topMargin + childLp.bottomMargin;
                totalHeight += childHeight + mVGap;
            }

        }
        totalHeight = totalHeight - mVGap;
        Log.i(TAG, "totalHeight " + totalHeight);
        if (childOffset == 1) {
            bgImageView.measure(MeasureSpec
                            .makeMeasureSpec(mTotalSize + getPaddingLeft() + getPaddingRight(),
                                    MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(
                            totalHeight + getPaddingTop() + getPaddingBottom(),
                            MeasureSpec.EXACTLY));
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), totalHeight + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int lastLeft = getPaddingLeft();
        int lastTop = getPaddingTop();
        int lastLineHeight = 0;
        int lastSpanGroupIndex = 0;
        int childOffset = bgImageView != null && indexOfChild(bgImageView) == 0 ? 1 : 0;
        if (childOffset == 1) {
            bgImageView.layout(0, 0, bgImageView.getMeasuredWidth(), bgImageView.getMeasuredHeight());
        }
        for (int i = 0; i < childCount - childOffset; i++) {
            int cellIndex = i + childOffset;
            int spanGroupIndex = mSpanSizeLookup.getSpanGroupIndex(i, mSpanCount);
            if (spanGroupIndex != lastSpanGroupIndex) {
                lastSpanGroupIndex = spanGroupIndex;
                lastLeft = getPaddingLeft();
                lastTop += lastLineHeight + mVGap;
                lastLineHeight = 0;
            }
            View child = getChildAt(cellIndex);
            VirtualLayoutManager.LayoutParams lp = (VirtualLayoutManager.LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            child.layout(lastLeft + lp.leftMargin, lastTop + lp.topMargin,
                    lastLeft + childWidth + lp.leftMargin,
                    lastTop + childHeight + lp.topMargin);
            lastLeft += childWidth + mHGap + lp.leftMargin + lp.rightMargin;
            if (childHeight > lastLineHeight) {
                lastLineHeight = childHeight + lp.topMargin + lp.bottomMargin;
            }
        }
    }

    @Override
    public void cellInited(BaseCell cell) {
        super.cellInited(cell);
        this.column = Utils.getCardColumnCount(cell.optIntParam(Card.KEY_TYPE));
    }

    @Override
    public void postBindView(BaseCell cell) {
        super.postBindView(cell);
        if (cell.style instanceof GridEntityCard.GridEntityStyle) {
            GridEntityCard.GridEntityStyle gridStyle = (GridEntityCard.GridEntityStyle) cell.style;

            setItemCount(mEntityCard.getCells().size());
            setSpanCount(column);
            int totalColumn = column;
            if (gridStyle.column > 0) {
                totalColumn = gridStyle.column;
                setSpanCount(gridStyle.column);
            }

            setSpanSizeLookup(new GridCard.CellSpanSizeLookup(mEntityCard.getCells(), totalColumn));

            setVGap(gridStyle.vGap);
            setHGap(gridStyle.hGap);
            setAutoExpand(gridStyle.autoExpand);
            setPadding(gridStyle.padding[Style.MARGIN_LEFT_INDEX],
                    gridStyle.padding[Style.MARGIN_TOP_INDEX],
                    gridStyle.padding[Style.MARGIN_RIGHT_INDEX],
                    gridStyle.padding[Style.MARGIN_BOTTOM_INDEX]);

            if (gridStyle.cols != null && gridStyle.cols.length > 0) {
                setWeights(gridStyle.cols);
            } else {
                setWeights(null);
            }

            if (!Float.isNaN(gridStyle.aspectRatio))
                setAspectRatio(gridStyle.aspectRatio);

            if (!TextUtils.isEmpty(gridStyle.bgImgUrl)) {
                if (bgImageView == null) {
                    bgImageView = ImageUtils.createImageInstance(getContext());
                    bgImageView.setLayoutParams(new VirtualLayoutManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                }
                addView(bgImageView, 0);
                CardSupport cardSupport = cell.serviceManager.getService(CardSupport.class);
                if (cardSupport != null) {
                    cardSupport.onBindBackgroundView(bgImageView, (EntityCard) cell);
                } else {
                    ImageUtils.doLoadImageUrl(bgImageView, cell.style.bgImgUrl);
                }
            }
        }

    }

    @Override
    public void postUnBindView(BaseCell cell) {
        super.postUnBindView(cell);
        if (bgImageView != null) {
            removeView(bgImageView);
        }
    }

    static final class DefaultSpanSizeLookup extends GridLayoutHelper.SpanSizeLookup {

        @Override
        public int getSpanSize(int position) {
            return 1;
        }

        @Override
        public int getSpanIndex(int position, int spanCount) {
            return position % spanCount;
        }
    }
}
