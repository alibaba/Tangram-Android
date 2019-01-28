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

package com.tmall.wireless.tangram.structure.cell;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tmall.wireless.tangram.core.R;
import com.tmall.wireless.tangram.core.adapter.BinderViewHolder;
import com.tmall.wireless.tangram.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kunlun on 9/17/16.
 */
public class LinearScrollCell extends BaseCell {
    /**
     * width of each item
     */
    public static final String KEY_PAGE_WIDTH = "pageWidth";
    /**
     * height of each item
     */
    public static final String KEY_PAGE_HEIGHT = "pageHeight";
    /**
     * background of indicator
     */
    public static final String KEY_DEFAULT_INDICATOR_COLOR = "defaultIndicatorColor";
    /**
     * foreground of indicator
     */
    public static final String KEY_INDICATOR_COLOR = "indicatorColor";

    /**
     * measure
     */
    public static final String KEY_INDICATOR_HEIGHT = "indicatorHeight";
    public static final String KEY_INDICATOR_WIDTH = "indicatorWidth";
    public static final String KEY_DEFAULT_INDICATOR_WIDTH = "defaultIndicatorWidth";
    public static final String KEY_INDICATOR_MARGIN = "indicatorMargin";

    public static final String KEY_HAS_INDICATOR = "hasIndicator";
    public static final String KEY_FOOTER_TYPE = "footerType";
    public static final String KEY_RETAIN_SCROLL_STATE = "retainScrollState";

    public static final String KEY_SCROLL_MARGIN_LEFT = "scrollMarginLeft";
    public static final String KEY_SCROLL_MARGIN_RIGHT = "scrollMarginRight";


    public static final String KEY_MAX_ROWS = "maxRows";
    public static final String KEY_MAX_COLS = "maxCols";

    public static final int DEFAULT_DEFAULT_INDICATOR_COLOR = Color.parseColor("#80ffffff");
    public static final int DEFAULT_INDICATOR_COLOR = Color.parseColor("#ffffff");
    public static final int DEFAULT_MAX_ROWS = 1;
    public static final int DEFAULT_INDICATOR_WIDTH = Style.parseSize("40rp", 0);
    public static final int DEFAULT_DEFAULT_INDICATOR_WIDTH = Style.parseSize("80rp", 0);
    public static final int DEFAULT_INDICATOR_HEIGHT = Style.parseSize("4rp", 0);
    public static final int DEFAULT_INDICATOR_MARGIN = Style.parseSize("14rp", 0);

    public static final String KEY_HGAP = "hGap";
    public static final String KEY_VGAP = "vGap";

    public List<BaseCell> cells = new ArrayList<BaseCell>();

    public BaseCell mHeader;
    public BaseCell mFooter;

    public double pageWidth = Double.NaN;
    public double pageHeight = Double.NaN;
    public int defaultIndicatorColor = DEFAULT_DEFAULT_INDICATOR_COLOR;
    public int indicatorColor = DEFAULT_INDICATOR_COLOR;
    public double indicatorWidth = Double.NaN;
    public double indicatorHeight = Double.NaN;
    public double defaultIndicatorWidth = Double.NaN;
    public boolean hasIndicator = true;
    public String footerType;
    public Adapter adapter;
    public int maxRows;
    public int maxCols;
    public int bgColor = Color.TRANSPARENT;
    public int scrollMarginLeft;
    public int scrollMarginRight;

    public double hGap;
    public double vGap;
    public double indicatorMargin;

    // current distance that responding recycler view has scrolled.
    public int currentDistance = 0;

    public boolean retainScrollState = true;

    @Override
    public void onAdded() {
        super.onAdded();
        adapter = new Adapter(getAdapter());
    }

    public void setCells(List<BaseCell> cells) {
        this.cells.clear();
        if (cells != null && cells.size() > 0) {
            this.cells.addAll(cells);
        }
        adapter.notifyDataSetChanged();
    }

    public GroupBasicAdapter getAdapter() {
        if (serviceManager != null) {
            return serviceManager.getService(GroupBasicAdapter.class);
        }
        return null;
    }

    public RecyclerView.RecycledViewPool getRecycledViewPool() {
        if (serviceManager != null) {
            return serviceManager.getService(RecyclerView.RecycledViewPool.class);
        }
        return null;
    }

    public int getMapperPosition(int rawPosition) {
        int cellCount = cells == null ? 0 : cells.size();
        if (cellCount == 0) {
            return rawPosition;
        }

        int columnCount = (int)(cellCount * 1.0f / maxRows + 0.5f);
        int rowIndex = rawPosition / maxRows;
        int columnIndex = rawPosition % maxRows;

        return columnIndex * columnCount + rowIndex;
    }

    @SuppressWarnings("unchecked")
    public class Adapter extends RecyclerView.Adapter<BinderViewHolder> {
        private GroupBasicAdapter adapter;

        public Adapter(GroupBasicAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public BinderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(BinderViewHolder binderViewHolder, int position) {
            int mapperPosition = getMapperPosition(position);
            binderViewHolder.bind(cells.get(mapperPosition));
            BaseCell cell = cells.get(mapperPosition);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(binderViewHolder.itemView.getLayoutParams());
            if (!Double.isNaN(pageWidth)) {
                lp.width = (int) (pageWidth + 0.5);
            }
            if (!Double.isNaN(pageHeight)) {
                lp.height = (int) (pageHeight + 0.5);
            }
            int[] margins = {0, 0, 0, 0};
            if (cell.style != null) {
                margins = cell.style.margin;
            }
            lp.setMargins(margins[3], margins[0], margins[1], margins[2]);
            //chils cell's pageWidth override pageWidth in style
            if (cell.extras.has("pageWidth")) {
                lp.width = Style.parseSize(cell.extras.optString("pageWidth"), 0);
            }
            binderViewHolder.itemView.setLayoutParams(lp);
            binderViewHolder.itemView.setTag(R.id.TANGRAM_LINEAR_SCROLL_POS, mapperPosition);
        }

        @Override
        public int getItemViewType(int position) {
            return adapter.getItemType(cells.get(position));
        }

        @Override
        public int getItemCount() {
            return cells == null ? 0 : cells.size();
        }

        @Override
        public void onViewRecycled(BinderViewHolder holder) {
            // to call cell.unbind method.
            adapter.onViewRecycled(holder);
        }
    }
}
