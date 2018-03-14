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

package com.tmall.wireless.tangram.structure.card;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.OnePlusNLayoutHelper;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.style.ColumnStyle;

import org.json.JSONException;
import org.json.JSONObject;

public class OnePlusNCard extends Card {


    private static final float[] EMPTY_WEIGHTS = new float[0];

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        super.parseWith(data, resolver);
    }

    @Override
    public void parseStyle(JSONObject data) {
        style = new ColumnStyle();
        if (data != null) {
            style.parseWith(data);
        }
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

    private void ensureBlock(BaseCell cell) {
        if (cell != null) {
            if (cell.style.extras == null) {
                cell.style.extras = new JSONObject();
            }
            try {
                cell.style.extras.put(Style.KEY_DISPLAY, Style.DISPLAY_BLOCK);
            } catch (JSONException e) {
            }
        }
    }

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper oldHelper) {
        OnePlusNLayoutHelper layoutHelper;
        if (oldHelper instanceof OnePlusNLayoutHelper) {
            layoutHelper = (OnePlusNLayoutHelper) oldHelper;
        } else {
            layoutHelper = new OnePlusNLayoutHelper();
        }

        layoutHelper.setItemCount(mCells.size());
        if (mCells.size() == 1) {
            BaseCell isHeader = mCells.get(0);
            layoutHelper.setHasHeader(Style.DISPLAY_BLOCK.equalsIgnoreCase(isHeader.optStringParam(Style.KEY_DISPLAY)));
            layoutHelper.setHasFooter(false);
        } else if (mCells.size() >= 2) {
            BaseCell isHeader = mCells.get(0);
            layoutHelper.setHasHeader(Style.DISPLAY_BLOCK.equalsIgnoreCase(isHeader.optStringParam(Style.KEY_DISPLAY)));
            BaseCell isFooter = mCells.get(mCells.size() - 1);
            layoutHelper.setHasFooter(Style.DISPLAY_BLOCK.equalsIgnoreCase(isFooter.optStringParam(Style.KEY_DISPLAY)));
        }

        if (style instanceof ColumnStyle) {
            ColumnStyle columnStyle = (ColumnStyle) style;
            if (columnStyle.cols != null && columnStyle.cols.length > 0)
                layoutHelper.setColWeights(columnStyle.cols);
            else
                layoutHelper.setColWeights(EMPTY_WEIGHTS);

            if (!Float.isNaN(style.aspectRatio)) {
                layoutHelper.setAspectRatio(style.aspectRatio);
            }

            if (columnStyle.rows != null && columnStyle.rows.length > 0) {
                layoutHelper.setRowWeight(columnStyle.rows[0]);
            }

            layoutHelper.setBgColor(columnStyle.bgColor);
            layoutHelper.setMargin(style.margin[Style.MARGIN_LEFT_INDEX], style.margin[Style.MARGIN_TOP_INDEX],
                style.margin[Style.MARGIN_RIGHT_INDEX], style.margin[Style.MARGIN_BOTTOM_INDEX]);
            layoutHelper.setPadding(style.padding[Style.MARGIN_LEFT_INDEX], style.padding[Style.MARGIN_TOP_INDEX],
                style.padding[Style.MARGIN_RIGHT_INDEX], style.padding[Style.MARGIN_BOTTOM_INDEX]);
        }


        return layoutHelper;
    }

}
