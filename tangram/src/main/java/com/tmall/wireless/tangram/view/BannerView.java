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

package com.tmall.wireless.tangram.view;

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.ultraviewpager.UltraViewPager;
import com.tmall.wireless.tangram.core.R;
import com.tmall.wireless.tangram.core.adapter.BinderViewHolder;
import com.tmall.wireless.tangram.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.eventbus.BusSupport;
import com.tmall.wireless.tangram.eventbus.EventContext;
import com.tmall.wireless.tangram.ext.BannerListener;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.cell.BannerCell;
import com.tmall.wireless.tangram.structure.view.ITangramViewLifeCycle;
import com.tmall.wireless.tangram.support.BannerSupport;
import com.tmall.wireless.tangram.util.ImageUtils;
import com.tmall.wireless.tangram.util.Utils;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikeafc on 16/1/14.
 */
public class BannerView extends RelativeLayout implements ViewPager.OnPageChangeListener,
    ITangramViewLifeCycle {

    private static final String CURRENT_POS = "__current_pos__";

    public static final int GRAVITY_LEFT = 0;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_RIGHT = 2;

    private UltraViewPager mUltraViewPager;
    private BannerIndicator mIndicator;
    private LayoutParams mIndicatorLayoutParams;

    private int mIndicatorHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int mIndicatorGap = Style.dp2px(6);
    private int mIndicatorMargin = Style.dp2px(10);

    private float xDown;
    private float yDown;

    private BaseCell cell;

    private BannerSupport bannerSupport;

    private List<BinderViewHolder> mViewHolders = new ArrayList<BinderViewHolder>();

    private int currentItemPos;

    private boolean init;

    private int direction; // 1 for right, -1 for left

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mUltraViewPager = new UltraViewPager(getContext());
        mUltraViewPager.setId(R.id.TANGRAM_BANNER_ID);
        mIndicator = new BannerIndicator(getContext());
        addView(mUltraViewPager);
        addView(mIndicator, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mIndicatorLayoutParams = (LayoutParams) mIndicator.getLayoutParams();
        mIndicatorLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.TANGRAM_BANNER_ID);
        mIndicator.setPadding(mIndicatorGap, 0, 0, 0);
    }

    public void setAdapter(PagerAdapter adapter) {
        mUltraViewPager.setAdapter(adapter);
        mUltraViewPager.disableAutoScroll();//reset timer when reuse
        mUltraViewPager.setOnPageChangeListener(this);
    }

    public void updateIndicators(String focusUrl, String norUrl, int radius, int focusColor, int norColor) {
        if (mIndicator != null) {
            mIndicator.updateIndicators(focusUrl, norUrl, radius, focusColor, norColor);
        }
    }

    public void setIndicatorGravity(int gravity) {
        switch (gravity) {
            case GRAVITY_LEFT:
                if (mIndicator != null) {
                    mIndicator.setGravity(Gravity.LEFT);
                }
                break;
            case GRAVITY_CENTER:
                if (mIndicator != null) {
                    mIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                break;
            case GRAVITY_RIGHT:
                if (mIndicator != null) {
                    mIndicator.setGravity(Gravity.RIGHT);
                }
                break;
        }
    }

    public void setIndicatorPos(String isInside) {
        if ("inside".equals(isInside)) {
            if (Build.VERSION.SDK_INT >= 17) {
                mIndicatorLayoutParams.removeRule(RelativeLayout.BELOW);
            }
            mIndicatorLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.TANGRAM_BANNER_ID);
        } else if ("outside".equals(isInside)) {
            if (Build.VERSION.SDK_INT >= 17) {
                mIndicatorLayoutParams.removeRule(RelativeLayout.ALIGN_BOTTOM);
            }
            mIndicatorLayoutParams.addRule(RelativeLayout.BELOW, R.id.TANGRAM_BANNER_ID);
        } else {
            if (Build.VERSION.SDK_INT >= 17) {
                mIndicatorLayoutParams.removeRule(RelativeLayout.BELOW);
            }
            mIndicatorLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.TANGRAM_BANNER_ID);
        }
    }

    public void setIndicatorGap(int gap) {
        if (gap > 0) {
            this.mIndicatorGap = gap;
        }
    }

    public void setIndicatorMargin(int indicatorMargin) {
        if (indicatorMargin > 0)
            this.mIndicatorMargin = indicatorMargin;
    }

    public void setIndicatorHeight(int indicatorHeight) {
        if (indicatorHeight > 0) {
            mIndicatorHeight = indicatorHeight;
        } else {
            mIndicatorHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }


    public UltraViewPager getUltraViewPager() {
        return mUltraViewPager;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (bannerSupport != null) {
            for (int j = 0; j < bannerSupport.getListeners().size(); j++) {
                BannerListener listener = bannerSupport.getListeners().get(j);
                listener.onPageScrolled(currentItemPos, positionOffset, positionOffsetPixels, direction);
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        currentItemPos = mUltraViewPager.getCurrentItem();
        mIndicator.setCurrItem(currentItemPos);

        if (cell != null && cell.extras != null) {
            try {
                cell.extras.put(CURRENT_POS, currentItemPos);
            } catch (JSONException e) {
            }
        }

        if (bannerSupport != null) {
            for (int j = 0; j < bannerSupport.getListeners().size(); j++) {
                BannerListener listener = bannerSupport.getListeners().get(j);
                listener.onPageSelected(position);
            }
        }
        if (cell != null && cell.serviceManager != null) {
            BusSupport busSupport = cell.serviceManager.getService(BusSupport.class);
            if (busSupport != null) {
                EventContext eventContext = new EventContext();
                if (((BannerCell)cell).mCells != null && currentItemPos >= 0
                    && currentItemPos < ((BannerCell)cell).mCells.size()) {
                    eventContext.producer = ((BannerCell)cell).mCells.get(currentItemPos);
                }
                busSupport.post(BusSupport.obtainEvent(BusSupport.EVENT_ON_EXPOSURE, cell.id, null, eventContext));
            }
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (bannerSupport != null) {
            for (int j = 0; j < bannerSupport.getListeners().size(); j++) {
                BannerListener listener = bannerSupport.getListeners().get(j);
                listener.onPageScrollStateChanged(state);
            }
        }
    }

    @Override
    public void cellInited(BaseCell cell) {
        this.init = (this.cell != cell);
        this.cell = cell;
    }

    @Override
    public void postBindView(BaseCell cell) {
        BannerCell bannerCell = (BannerCell) cell;
        bannerCell.initAdapter();
        if (cell.style != null)
            setPadding(cell.style.padding[3], cell.style.padding[0], cell.style.padding[1], cell.style.padding[2]);
        setBackgroundColor(bannerCell.mBgColor);
        setAdapter(bannerCell.mBannerAdapter);
        getUltraViewPager().setAutoMeasureHeight(true);
        getUltraViewPager().setRatio(bannerCell.mRatio);
        getUltraViewPager().setAutoScroll(bannerCell.mAutoScrollInternal, bannerCell.mSpecialInterval);
        getUltraViewPager().getViewPager().setPageMargin(bannerCell.hGap);
        if (bannerCell.mCells.size() <= bannerCell.mInfiniteMinCount) {
            getUltraViewPager().setInfiniteLoop(false);
        } else {
            getUltraViewPager().setInfiniteLoop(bannerCell.mInfinite);
        }
        setIndicatorGravity(getIndicatorGravity(bannerCell.mIndicatorGravity));
        setIndicatorPos(bannerCell.mIndicatorPos);
        int indicatorGap = bannerCell.mIndicatorGap;
        if (indicatorGap <= 0) {
            indicatorGap = mIndicatorGap;
        }
        setIndicatorGap(indicatorGap);
        int indicatorMargin = bannerCell.mIndicatorMargin;
        if (indicatorMargin <= 0) {
            indicatorMargin = mIndicatorMargin;
        }
        setIndicatorMargin(indicatorMargin);
        int indicatorHeight = bannerCell.mIndicatorHeight;
        setIndicatorHeight(indicatorHeight);
        if (bannerCell.itemMargin[0] > 0 || bannerCell.itemMargin[1] > 0) {
            getUltraViewPager().setScrollMargin(bannerCell.itemMargin[0], bannerCell.itemMargin[1]);
            getUltraViewPager().getViewPager().setClipToPadding(false);
            getUltraViewPager().getViewPager().setClipChildren(false);
        } else {
            getUltraViewPager().setScrollMargin(0, 0);
            getUltraViewPager().getViewPager().setClipToPadding(true);
            getUltraViewPager().getViewPager().setClipChildren(true);
        }
        VirtualLayoutManager.LayoutParams layoutParams = (VirtualLayoutManager.LayoutParams) getLayoutParams();
        layoutParams.setMargins(bannerCell.margin[3], bannerCell.margin[0], bannerCell.margin[1], bannerCell.margin[2]);
        getUltraViewPager().setItemRatio(bannerCell.itemRatio);
        currentItemPos = bannerCell.optIntParam(CURRENT_POS);
        getUltraViewPager().setCurrentItem(currentItemPos);
        updateIndicators(bannerCell.mIndicatorFocus, bannerCell.mIndicatorNor,
            bannerCell.mIndicatorRadius, bannerCell.mIndicatorColor,
            bannerCell.mIndicatorDefaultColor);
        recycleView();
        bindHeaderView(bannerCell.mHeader);
        bindFooterView(bannerCell.mFooter);
        if (cell.serviceManager != null) {
            bannerSupport = cell.serviceManager.getService(BannerSupport.class);
        }
    }

    @Override
    public void postUnBindView(BaseCell cell) {
        recycleView();
    }

    private int getIndicatorGravity(String gravity) {
        if ("left".equals(gravity))
            return BannerView.GRAVITY_LEFT;
        if ("right".equals(gravity))
            return BannerView.GRAVITY_RIGHT;
        return BannerView.GRAVITY_CENTER;
    }

    class BannerIndicator extends LinearLayout {

        private final int STYLE_NONE = 0;
        private final int STYLE_DOT = 1;
        private final int STYLE_IMG = 2;

        private ImageView[] mImageViews;
        private String focusUrl;
        private String norUrl;
        private int norColor;
        private int focusColor;
        private float radius;
        private int style;

        public BannerIndicator(Context context) {
            super(context);
        }

        public void updateIndicators(String focusUrl, String norUrl, int radius, int focusColor, int norColor) {
            if (mUltraViewPager.getAdapter() == null)
                return;

            this.focusUrl = focusUrl;
            this.norUrl = norUrl;

            this.norColor = norColor;
            this.focusColor = focusColor;
            this.radius = radius;

            if (norColor != 0 && focusColor != 0 && radius > 0) {
                style = STYLE_DOT;
            } else if (!TextUtils.isEmpty(focusUrl) && !TextUtils.isEmpty(norUrl)) {
                style = STYLE_IMG;
            } else {
                style = STYLE_NONE;
            }
            if (style == STYLE_NONE) {
                setVisibility(INVISIBLE);
                return;
            } else {
                setVisibility(VISIBLE);
            }
            int width = 0;
            int height = 0;

            if (style == STYLE_IMG) {
                Pair<Integer, Integer> norSize = Utils.getImageSize(norUrl);
                Pair<Integer, Integer> focSize = Utils.getImageSize(focusUrl);
                if (norSize != null && focSize != null) {
                    width = Math.max(norSize.first, focSize.first);
                    height = Math.max(norSize.second, focSize.second);
                } else {
                    if (focSize != null) {
                        width = focSize.first;
                        height = focSize.second;
                    }
                    if (norSize != null) {
                        width = norSize.first;
                        height = norSize.second;
                    }
                }
            } else if (style == STYLE_DOT) {
                width = 2 * radius;
                height = 2 * radius;
            }
            if (mIndicatorHeight != ViewGroup.LayoutParams.WRAP_CONTENT && mIndicatorHeight > 0) {
                height = mIndicatorHeight;
            }

            int count = mUltraViewPager.getAdapter().getCount();
            if (mImageViews == null) {
                mImageViews = new ImageView[count];
                for (int i = 0; i < mImageViews.length; i++) {
                    mImageViews[i] = ImageUtils.createImageInstance(getContext());
                    mImageViews[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    addView(mImageViews[i]);
                }
            } else if (mImageViews.length != count) {
                for (int i = 0; i < mImageViews.length; i++) {
                    removeView(mImageViews[i]);
                }
                ImageView[] old = mImageViews;
                mImageViews = new ImageView[count];
                System.arraycopy(old, 0,mImageViews, 0, Math.min(old.length, count));
                for (int i = 0; i < mImageViews.length; i++) {
                    if (mImageViews[i] == null) {
                        mImageViews[i] = ImageUtils.createImageInstance(getContext());
                        mImageViews[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                    addView(mImageViews[i]);
                }
            }
            int position = mUltraViewPager.getCurrentItem();
            for (int i = 0; i < mImageViews.length; i++) {
                LinearLayout.LayoutParams layoutParams = (LayoutParams) mImageViews[i].getLayoutParams();
                if (style == STYLE_IMG || style == STYLE_DOT) {
                    layoutParams.setMargins(0, mIndicatorMargin, mIndicatorGap, mIndicatorMargin);
                    if (width > 0)
                        layoutParams.width = width;
                    if (height > 0)
                        layoutParams.height = height;
                } else {
                    layoutParams.setMargins(0, 0, 0, 0);
                }
                if (style == STYLE_DOT) {
                    mImageViews[i].setImageDrawable(getGradientDrawable(position == i ? focusColor : norColor, radius));
                } else if (style == STYLE_IMG){
                    //ImageUtils.doLoadImageUrl(mImageViews[i], position == i ? focusUrl : norUrl);
                }
            }
			if (style == STYLE_IMG) {
	            if (init) {
	                for (int i = 0; i < mImageViews.length; i++) {
	                    ImageUtils.doLoadImageUrl(mImageViews[i], position == i ? focusUrl : norUrl);
                        if (i == currentItemPos) {
                            mImageViews[i].setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, currentItemPos);
                        }
                    }
	            } else {
                    for (int i = 0; i < mImageViews.length; i++) {
                        ImageView imageView = mImageViews[i];
                        if (imageView.getTag(R.id.TANGRAM_BANNER_INDICATOR_POS) == null) {
                            continue;
                        } else {
                            imageView.setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, null);
                            ImageUtils.doLoadImageUrl(imageView, norUrl);
                        }
                    }
                    mImageViews[currentItemPos].setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, currentItemPos);
                    ImageUtils.doLoadImageUrl(mImageViews[currentItemPos], focusUrl);
				}
			}
        }

        public void setCurrItem(int position) {
            if (mImageViews != null) {
                for (int i = 0; i < mImageViews.length; i++) {
                    if (style == STYLE_DOT) {
                        mImageViews[i].setImageDrawable(getGradientDrawable(position == i ? focusColor : norColor, radius));
                    } else if (style == STYLE_IMG){
                        ImageView imageView = mImageViews[i];
                        if (imageView.getTag(R.id.TANGRAM_BANNER_INDICATOR_POS) == null) {
                            continue;
                        } else {
                            imageView.setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, null);
                            ImageUtils.doLoadImageUrl(imageView, norUrl);
                        }
                    }
                }
                mImageViews[currentItemPos].setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, currentItemPos);
                ImageUtils.doLoadImageUrl(mImageViews[currentItemPos], focusUrl);
            }
        }

        private GradientDrawable getGradientDrawable(int color, float radius) {
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{color, color});
            gradientDrawable.setShape(GradientDrawable.OVAL);
            gradientDrawable.setCornerRadius(radius);
            return gradientDrawable;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int action = ev.getAction();
        float x = ev.getRawX();
        float y = ev.getRawY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                xDown = x;
                yDown = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int xDiff = (int) (x - xDown);
                int yDiff = (int) (y - yDown);

                direction = -xDiff;

                if (Math.abs(xDiff) >= Math.abs(yDiff)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                direction = 0;
                break;
        }

        return false;
    }

    private void bindHeaderView(BaseCell cell) {
        if (cell != null) {
            View header = getViewFromRecycler(cell);
            if (header != null) {
                header.setId(R.id.TANGRAM_BANNER_HEADER_ID);
                RelativeLayout.LayoutParams bodyLp = (LayoutParams) mUltraViewPager.getLayoutParams();
                bodyLp.addRule(RelativeLayout.BELOW, R.id.TANGRAM_BANNER_HEADER_ID);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = cell.style.margin[Style.MARGIN_TOP_INDEX];
                lp.leftMargin = cell.style.margin[Style.MARGIN_LEFT_INDEX];
                lp.bottomMargin = cell.style.margin[Style.MARGIN_BOTTOM_INDEX];
                lp.rightMargin = cell.style.margin[Style.MARGIN_RIGHT_INDEX];
                addView(header, lp);
            }
        }
    }

    private void bindFooterView(BaseCell cell) {
        if (cell != null) {
            View footer = getViewFromRecycler(cell);
            if (footer != null) {
                footer.setId(R.id.TANGRAM_BANNER_FOOTER_ID);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.BELOW, R.id.TANGRAM_BANNER_ID);
                lp.topMargin = cell.style.margin[Style.MARGIN_TOP_INDEX];
                lp.leftMargin = cell.style.margin[Style.MARGIN_LEFT_INDEX];
                lp.bottomMargin = cell.style.margin[Style.MARGIN_BOTTOM_INDEX];
                lp.rightMargin = cell.style.margin[Style.MARGIN_RIGHT_INDEX];
                addView(footer, lp);
            }
        }
    }

    private View getViewFromRecycler(@NonNull BaseCell cell) {
        GroupBasicAdapter adapter = cell.serviceManager.getService(GroupBasicAdapter.class);
        RecyclerView.RecycledViewPool pool = cell.serviceManager.getService(RecyclerView.RecycledViewPool.class);
        int itemViewType = adapter.getItemType(cell);
        BinderViewHolder holder = (BinderViewHolder) pool.getRecycledView(itemViewType);
        if (holder == null) {
            holder = (BinderViewHolder) adapter.createViewHolder(this, itemViewType);
        }
        holder.bind(cell);
        mViewHolders.add(holder);
        return holder.itemView;
    }

    private void recycleView() {
        if (!mViewHolders.isEmpty()) {
            RecyclerView.RecycledViewPool pool = cell.serviceManager.getService(RecyclerView.RecycledViewPool.class);
            for (int i = 0, size = mViewHolders.size(); i < size; i++) {
                BinderViewHolder viewHolder = mViewHolders.get(i);
                viewHolder.unbind();
                removeView(viewHolder.itemView);
                pool.putRecycledView(viewHolder);
            }
            mViewHolders.clear();
        }
    }

}
