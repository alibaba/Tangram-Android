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

package com.tmall.wireless.tangram.structure.card;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Card perform grid layout
 *
 * @author villadora
 * @since 1.0.0
 */
public class GridCard extends Card {

    private static final String TAG = "GridCard";

    private int mColumn = 0;

    public GridCard() {

    }

    public GridCard(int column) {
        this.mColumn = column;
    }

    @Override
    protected void parseHeaderCell(@NonNull MVHelper resolver, @Nullable JSONObject header) {
        BaseCell mHeader = createCell(resolver, header, true);
        ensureBlock(mHeader);
    }

    @Override
    protected void parseFooterCell(@NonNull MVHelper resolver, @Nullable JSONObject footer) {
        BaseCell mFooter = createCell(resolver, footer, true);
        ensureBlock(mFooter);
    }

    @Override
    protected void parseStyle(JSONObject data) {
        style = new GridStyle();
        if (data != null) {
            style.parseWith(data);
        }
        if (((GridStyle) style).column > 0) {
            mColumn = ((GridStyle) style).column;
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (mColumn > 0 || (style instanceof GridStyle && ((GridStyle) style).column > 0));
    }

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper helper) {

        GridLayoutHelper gridHelper;
        if (helper instanceof GridLayoutHelper) {
            gridHelper = (GridLayoutHelper) helper;
        } else {
            gridHelper = new GridLayoutHelper(1, mCells.size());
        }


        gridHelper.setItemCount(mCells.size());
        gridHelper.setSpanCount(mColumn);


        // update style
        if (style instanceof GridStyle) {
            GridStyle gridStyle = (GridStyle) style;

            int totalColumn = mColumn;
            if (gridStyle.column > 0) {
                totalColumn = gridStyle.column;
                gridHelper.setSpanCount(gridStyle.column);
            }

            gridHelper.setSpanSizeLookup(new CellSpanSizeLookup(mCells, totalColumn));

            gridHelper.setVGap(gridStyle.vGap);
            gridHelper.setHGap(gridStyle.hGap);
            gridHelper.setAutoExpand(gridStyle.autoExpand);

            if (gridStyle.cols != null && gridStyle.cols.length > 0) {
                gridHelper.setWeights(gridStyle.cols);
            }

            if (!Float.isNaN(gridStyle.aspectRatio))
                gridHelper.setAspectRatio(gridStyle.aspectRatio);
        }

        return gridHelper;
    }

    private void ensureBlock(BaseCell cell) {
        if (cell != null) {
            if (cell.style.extras == null) {
                cell.style.extras = new JSONObject();
            }
            try {
                cell.style.extras.put(Style.KEY_DISPLAY, Style.DISPLAY_BLOCK);
            } catch (JSONException e) {
                Log.w(TAG, Log.getStackTraceString(e), e);
            }
        }
    }

    public static class GridStyle extends Style {
        public static final String KEY_COLUMN = "column";

        public static final String KEY_AUTO_EXPAND = "autoExpand";

        public static final String KEY_IGNORE_EXTRA = "ignoreExtra";

        public static final String KEY_H_GAP = "hGap";
        public static final String KEY_V_GAP = "vGap";

        public int vGap = 0;
        public int hGap = 0;

        public boolean autoExpand = false;

        public int column = 0;

        public float[] cols;

        @Override
        public void parseWith(JSONObject data) {
            super.parseWith(data);

            if (data != null) {
                column = data.optInt(KEY_COLUMN, 0);

                autoExpand = data.optBoolean(KEY_AUTO_EXPAND, false);

                JSONArray jsonCols = data.optJSONArray(KEY_COLS);
                if (jsonCols != null) {
                    cols = new float[jsonCols.length()];
                    for (int i = 0; i < cols.length; i++) {
                        cols[i] = (float) jsonCols.optDouble(i, 0);
                    }
                } else {
                    cols = new float[0];
                }

                hGap = Style.dp2px(data.optDouble(KEY_H_GAP, 0.0f));
                vGap = Style.dp2px(data.optDouble(KEY_V_GAP, 0.0f));
            }
        }
    }

    public static class CellSpanSizeLookup extends GridLayoutHelper.SpanSizeLookup {

        public static final String DISPLAY_INLINE = "inline";
        public static final String DISPLAY_BLOCK = "block";

        public static final String KEY_COLSPAN = "colspan";
        public static final String KEY_DISPLAY = "display";

        protected final List<BaseCell> mCells;

        private final int mTotalColumn;


        public CellSpanSizeLookup(List<BaseCell> cells, int totalColumn) {
            this.mCells = cells;
            this.mTotalColumn = totalColumn;
        }

        @Override
        public int getSpanSize(int position) {
            position = position - getStartPosition();
            if (position < 0 || position >= mCells.size())
                return 0;

            BaseCell cell = mCells.get(position);

            if (cell != null && cell.style != null && cell.style.extras != null) {
                int column = cell.style.extras.optInt(KEY_COLSPAN, 1);

                String display = cell.style.extras.optString(KEY_DISPLAY, DISPLAY_INLINE);

                if (TextUtils.equals(DISPLAY_BLOCK, display)) {
                    return mTotalColumn;
                }

                return cell.style.extras.optInt(KEY_COLSPAN, 1);
            }

            return 1;
        }

    }

}
