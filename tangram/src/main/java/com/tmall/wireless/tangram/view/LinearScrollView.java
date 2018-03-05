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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tmall.wireless.tangram.core.R;
import com.tmall.wireless.tangram.core.adapter.BinderViewHolder;
import com.tmall.wireless.tangram.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.eventbus.BusSupport;
import com.tmall.wireless.tangram.ext.HorizontalOverScrollBounceEffectDecoratorExt;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.cell.LinearScrollCell;
import com.tmall.wireless.tangram.structure.view.ITangramViewLifeCycle;

import me.everything.android.ui.overscroll.adapters.StaticOverScrollDecorAdapter;

/**
 * Created by Kunlun on 9/17/16.
 */
public class LinearScrollView extends LinearLayout implements ITangramViewLifeCycle,
        HorizontalOverScrollBounceEffectDecoratorExt.OnMotionEventListener,
        HorizontalOverScrollBounceEffectDecoratorExt.OnOverScrollListener {
    private RecyclerView recyclerView;
    private View indicator, indicatorContainer;

    private LinearScrollCell lSCell;

    // total distance that indicator can move.
    private float totalDistanceOfIndicator = 0;
    // total distance that recycler view can scroll.
    private float totalDistance = 0;

    private int touchSlop;
    private HorizontalOverScrollBounceEffectDecoratorExt overScrollDecorator;
    private boolean enableOverScrollPull;

    private List<BinderViewHolder> mViewHolders = new ArrayList<BinderViewHolder>();

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (lSCell == null) {
                return;
            }

            lSCell.currentDistance += dx;

            if (lSCell.hasIndicator && totalDistance > 0) {
                float distance = Math.max(0, Math.min((int) (lSCell.currentDistance * totalDistanceOfIndicator
                        / totalDistance + 0.5), totalDistanceOfIndicator));
                indicator.setTranslationX(distance);
            }
        }
    };

    public LinearScrollView(Context context) {
        this(context, null);
    }

    public LinearScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        inflate(getContext(), R.layout.tangram_linearscrollview, this);
        setClickable(true);
        recyclerView = (RecyclerView) findViewById(R.id.tangram_linearscrollview_container);
        indicator = findViewById(R.id.tangram_linearscrollview_indicator);
        indicatorContainer = findViewById(R.id.tangram_linearscrollview_indicator_container);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        totalDistanceOfIndicator = Style.dp2px(34);

        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        overScrollDecorator = new HorizontalOverScrollBounceEffectDecoratorExt(new StaticOverScrollDecorAdapter(this));
    }

    private float xDown;
    private float yDown;

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
                int xDistance = (int) (x - xDown);
                int yDistance = (int) (y - yDown);

                if (Math.abs(xDistance) > touchSlop && Math.abs(xDistance) > Math.abs(yDistance)) {
                    if (!recyclerView.canScrollHorizontally(-1) && xDistance > 0
                            || (!recyclerView.canScrollHorizontally(1) && xDistance < 0)) {
                        enableOverScrollPull = true;
                        return true;
                    } else {
                        enableOverScrollPull = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (enableOverScrollPull) {
                    return true;
                }
                break;
            default:
                break;
        }

        return false;
    }

    @Override
    public void cellInited(BaseCell cell) {
        if (cell instanceof LinearScrollCell) {
            this.lSCell = (LinearScrollCell) cell;
        }
    }

    @Override
    public void postBindView(BaseCell cell) {
        if (lSCell == null) {
            return;
        }

        recyclerView.setRecycledViewPool(lSCell.getRecycledViewPool());

        float[] starts = null;
        if (lSCell.cells != null && lSCell.cells.size() > 0) {
            starts = new float[lSCell.cells.size()];
            for (int i = 0; i < lSCell.cells.size(); i++) {
                starts[i] = totalDistance;

                BaseCell bc = lSCell.cells.get(i);
                if (bc.style != null && bc.style.margin.length > 0) {
                    totalDistance = totalDistance + bc.style.margin[1] + bc.style.margin[3];
                }
                if (!Double.isNaN(lSCell.pageWidth)) {
                    if (bc.extras.has("pageWidth")) {
                        totalDistance += Style.parseSize(bc.extras.optString("pageWidth"), 0);
                    } else {
                        totalDistance += lSCell.pageWidth;
                    }
                }
            }
        }
        totalDistance -= getScreenWidth();

        // calculate height of recycler view.
        ViewGroup.LayoutParams lp = recyclerView.getLayoutParams();
        if (!Double.isNaN(lSCell.pageHeight)) {
            lp.height = (int) (lSCell.pageHeight + 0.5);
        }
        recyclerView.setLayoutParams(lp);

        recyclerView.setAdapter(lSCell.adapter);

        if (lSCell.hasIndicator && totalDistance > 0) {
            setViewColor(indicator, lSCell.indicatorColor);
            setViewColor(indicatorContainer, lSCell.defaultIndicatorColor);
            indicatorContainer.setVisibility(VISIBLE);
        } else {
            indicatorContainer.setVisibility(GONE);
        }

        overScrollDecorator.setOnOverScrollListener(this);
        overScrollDecorator.setOnMotionEventListener(this);

        recyclerView.addOnScrollListener(onScrollListener);

        setBackgroundColor(lSCell.bgColor);

        if (lSCell.retainScrollState) {
            int position = computeFirstCompletelyVisibleItemPositionForScrolledX(starts);
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            lm.scrollToPositionWithOffset(position,
                    starts == null || starts.length <= position ? 0 : (int) (starts[position] - lSCell.currentDistance));
        }
        if (lSCell.scrollMarginLeft > 0 || lSCell.scrollMarginRight > 0) {
            setPadding(lSCell.scrollMarginLeft, 0, lSCell.scrollMarginRight, 0);
            setClipToPadding(false);
            setClipChildren(false);
        } else {
            setPadding(0, 0, 0, 0);
            setClipToPadding(true);
            setClipChildren(true);
        }
        recycleView(lSCell);
        bindHeaderView(lSCell.mHeader);
        bindFooterView(lSCell.mFooter);
    }

    /**
     * Find the first completely visible position.
     *
     * @param starts A recorder array to save each item's left position, including its margin.
     * @return Position of first completely visible item.
     */
    private int computeFirstCompletelyVisibleItemPositionForScrolledX(float[] starts) {
        if (lSCell == null || starts == null || starts.length <= 0) {
            return 0;
        }
        for (int i = 0; i < starts.length; i++) {
            if (starts[i] > lSCell.currentDistance) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void postUnBindView(BaseCell cell) {
        if (lSCell == null) {
            return;
        }

        totalDistance = 0;

        if (lSCell.hasIndicator) {
            indicator.setTranslationX(0);
        }

        overScrollDecorator.setOnOverScrollListener(null);
        overScrollDecorator.setOnMotionEventListener(null);

        recyclerView.removeOnScrollListener(onScrollListener);
        recyclerView.setAdapter(null);

        lSCell = null;
        recycleView(cell);
    }

    private void setViewColor(View view, int color) {
        if (view.getBackground() instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) view.getBackground().mutate();
            drawable.setColor(color);
        }
    }

    private int getScreenWidth() {
        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        return configuration.orientation == Configuration.ORIENTATION_PORTRAIT ?
                dm.widthPixels : dm.heightPixels;
    }

    @Override
    public void onMotionEvent(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (lSCell != null && lSCell.serviceManager != null) {
                BusSupport busSupport = lSCell.serviceManager.getService(BusSupport.class);
                ArrayMap<String, String> params = new ArrayMap<String, String>();
                params.put("spmcOffset", String.valueOf(lSCell.cells.size()));
                busSupport.post(BusSupport.obtainEvent("onMotionEvent", null, params, null));
            }
        }
    }

    @Override
    public void onOverScroll(View view, float offset) {
        if (lSCell != null && lSCell.serviceManager != null) {
            BusSupport busSupport = lSCell.serviceManager.getService(BusSupport.class);
            ArrayMap<String, String> params = new ArrayMap<String, String>();
            params.put("offset", String.valueOf(offset));
            busSupport.post(BusSupport.obtainEvent("onOverScroll", null, params, null));
        }
    }

    private void bindHeaderView(BaseCell cell) {
        if (cell != null) {
            View header = getViewFromRecycler(cell);
            if (header != null) {
                header.setId(R.id.TANGRAM_BANNER_HEADER_ID);
                //为了解决在 item 复用过程中，itemView 的 layoutParams 复用造成 layout 错误,这里要提供一个新的 layoutParams。
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = cell.style.margin[Style.MARGIN_TOP_INDEX];
                lp.leftMargin = cell.style.margin[Style.MARGIN_LEFT_INDEX];
                lp.bottomMargin = cell.style.margin[Style.MARGIN_BOTTOM_INDEX];
                lp.rightMargin = cell.style.margin[Style.MARGIN_RIGHT_INDEX];
                addView(header, 0, lp);
            }
        }
    }

    private void bindFooterView(BaseCell cell) {
        if (cell != null) {
            View footer = getViewFromRecycler(cell);
            if (footer != null) {
                footer.setId(R.id.TANGRAM_BANNER_FOOTER_ID);
                //为了解决在 item 复用过程中，itemView 的 layoutParams 复用造成 layout 错误,这里要提供一个新的 layoutParams。
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
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

    private void recycleView(@NonNull BaseCell cell) {
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
