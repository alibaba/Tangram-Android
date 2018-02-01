package com.tmall.wireless.tangram.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.tmall.ultraviewpager.UltraViewPagerAdapter;

/**
 * Created by longerian on 2018/1/31.
 *
 * @author longerian
 * @date 2018/01/31
 */

public class BannerViewPager extends ViewPager implements UltraViewPagerAdapter.UltraViewPagerCenterListener {

    private UltraViewPagerAdapter pagerAdapter;

    private boolean enableLoop;
    private boolean autoMeasureHeight;
    private double itemRatio = Double.NaN;
    private int constrainLength;

    private int itemMarginLeft;
    private int itemMarginTop;
    private int itemMarginRight;
    private int itemMarginBottom;

    private float ratio = Float.NaN;

    public BannerViewPager(Context context) {
        super(context);
        init(context, null);
    }

    public BannerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setClipChildren(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        onMeasurePage(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onMeasurePage(int widthMeasureSpec, int heightMeasureSpec) {
        View child = pagerAdapter.getViewAtPosition(getCurrentItem());
        if (child == null) {
            child = getChildAt(0);
        }
        if (child == null) {
            return;
        }
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if ((view.getPaddingLeft() != itemMarginLeft ||
                view.getPaddingTop() != itemMarginTop ||
                view.getPaddingRight() != itemMarginRight ||
                view.getPaddingBottom() != itemMarginBottom)) {
                view.setPadding(itemMarginLeft, itemMarginTop, itemMarginRight, itemMarginBottom);
            }
        }

        ViewGroup.LayoutParams lp = child.getLayoutParams();
        final int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
        final int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, lp.height);

        int childWidth = (int) ((MeasureSpec.getSize(childWidthSpec) - getPaddingLeft() - getPaddingRight()) * pagerAdapter.getPageWidth(getCurrentItem()));
        int childHeight = 0;

        if (!Double.isNaN(itemRatio)) {
            int itemHeight = (int) (childWidth / itemRatio);
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                view.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY));
            }
        } else {
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                if (pagerAdapter.getPageWidth(getCurrentItem()) != 1) {
                    view.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                } else {
                    view.measure(childWidthSpec, childHeightSpec);
                }
            }
        }

        childWidth = itemMarginLeft + child.getMeasuredWidth() + itemMarginRight;
        childHeight = itemMarginTop + child.getMeasuredHeight() + itemMarginBottom;

        if (!Float.isNaN(ratio)) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (getMeasuredWidth() / ratio), MeasureSpec.EXACTLY);
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                view.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        } else {
            if (autoMeasureHeight) {
                constrainLength = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
                setMeasuredDimension(getMeasuredWidth(), childHeight);
            }
        }

        if (!pagerAdapter.isEnableMultiScr()) {
            return;
        }

        int pageLength = getMeasuredWidth();

        final int childLength = child.getMeasuredWidth();

        // Check that the measurement was successful
        if (childLength > 0) {
            int difference = pageLength - childLength;
            if (getPageMargin() == 0) {
                setPageMargin(-difference);
            }
            int offscreen = (int) Math.ceil((float) pageLength / (float) childLength) + 1;
            setOffscreenPageLimit(offscreen);
            requestLayout();
        }
    }

    public PagerAdapter getWrapperAdapter() {
        return super.getAdapter() == null ? null : ((UltraViewPagerAdapter) super.getAdapter()).getAdapter();
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        if (adapter != null) {
            if (pagerAdapter == null || pagerAdapter != adapter) {
                pagerAdapter = (UltraViewPagerAdapter)adapter;
                pagerAdapter.setCenterListener(this);
                pagerAdapter.setEnableLoop(enableLoop);
                //pagerAdapter.setMultiScrRatio(multiScrRatio);
                constrainLength = 0;
                super.setAdapter(pagerAdapter);
            }
        } else {
            super.setAdapter(adapter);
        }
    }

    @Override
    public void setCurrentItem(int item) {
        setCurrentItem(item, false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (pagerAdapter.getCount() != 0 && pagerAdapter.isEnableLoop()) {
            item = pagerAdapter.getCount() / 2 + item % pagerAdapter.getRealCount();
        }
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public int getCurrentItem() {
        if (pagerAdapter != null && pagerAdapter.getCount() != 0) {
            int position = super.getCurrentItem();
            return position % pagerAdapter.getRealCount();
        }
        return super.getCurrentItem();
    }

    public int getNextItem() {
        if (pagerAdapter != null && pagerAdapter.getCount() != 0) {
            int next = super.getCurrentItem() + 1;
            return next % pagerAdapter.getRealCount();
        }
        return 0;
    }

    /**
     * Set the currently selected page.
     *
     * @param item         Item index to select
     * @param smoothScroll True to smoothly scroll to the new item, false to transition immediately
     */
    public void setCurrentItemFake(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    /**
     * Get the currently selected page.
     */
    public int getCurrentItemFake() {
        return super.getCurrentItem();
    }

    public void setEnableLoop(boolean status) {
        enableLoop = status;
        if (pagerAdapter != null) {
            pagerAdapter.setEnableLoop(enableLoop);
        }
    }

    public void setItemRatio(double itemRatio) {
        this.itemRatio = itemRatio;
    }

    public void setAutoMeasureHeight(boolean autoMeasureHeight) {
        this.autoMeasureHeight = autoMeasureHeight;
    }

    public int getConstrainLength() {
        return constrainLength;
    }

    public void setItemMargin(int left, int top, int right, int bottom) {
        itemMarginLeft = left;
        itemMarginTop = top;
        itemMarginRight = right;
        itemMarginBottom = bottom;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    @Override
    public void center() {
        setCurrentItem(0);
    }

    @Override
    public void resetPosition() {
        setCurrentItem(getCurrentItem());
    }

}
