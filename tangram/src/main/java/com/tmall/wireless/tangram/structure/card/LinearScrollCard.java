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

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.MVResolver;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.cell.LinearScrollCell;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Kunlun on 9/17/16.
 */
public class LinearScrollCard extends Card {
    private static final String LOG_TAG = "LinearScrollCard";

    private LinearScrollCell cell = new LinearScrollCell();

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        super.parseWith(data, resolver);
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", TangramBuilder.TYPE_LINEAR_SCROLL_CELL);
            obj.put("bizId", id);

            resolver.parseCell(resolver, cell, obj);

            if (!super.getCells().isEmpty()) {
                cell.cells.addAll(super.getCells());
                super.setCells(Collections.<BaseCell>singletonList(cell));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            setCells(null);
        }
    }

    @Override
    protected void parseHeaderCell(@NonNull MVHelper resolver, @Nullable JSONObject header) {
        cell.mHeader = createCell(resolver, header, false);
        if (cell.mHeader != null) {
            cell.mHeader.parent = this;
            cell.mHeader.parentId = id;
            cell.mHeader.pos = 0;
            try {
                cell.mHeader.extras.put(MVResolver.KEY_INDEX, cell.mHeader.pos);
            } catch (JSONException e) {
            }
        }
    }

    @Override
    protected void parseFooterCell(@NonNull MVHelper resolver, @Nullable JSONObject footer) {
        cell.mFooter = createCell(resolver, footer, false);
        if (cell.mFooter != null) {
            cell.mFooter.parent = this;
            cell.mFooter.parentId = id;
            cell.mFooter.pos = cell.mHeader != null ? getCells().size() + 1: getCells().size();
            try {
                cell.mFooter.extras.put(MVResolver.KEY_INDEX, cell.mFooter.pos);
            } catch (JSONException e) {
            }
        }
    }

    @Override
    public void setCells(@Nullable List<BaseCell> cells) {
        if (cells == null || cells.isEmpty()) {
            super.setCells(null);
        } else {
            cell.setCells(cells);
            super.setCells(Collections.<BaseCell>singletonList(cell));
        }
        notifyDataChange();
    }

    @Override
    public void parseStyle(@Nullable JSONObject data) {
        super.parseStyle(data);
        if (data != null) {
            cell.pageWidth = Style.parseSize(optStringParam(LinearScrollCell.KEY_PAGE_WIDTH), 0);
            cell.pageHeight = Style.parseSize(optStringParam(LinearScrollCell.KEY_PAGE_HEIGHT), 0);
            cell.defaultIndicatorColor = parseColor(optStringParam(LinearScrollCell.KEY_DEFAULT_INDICATOR_COLOR),
                LinearScrollCell.DEFAULT_DEFAULT_INDICATOR_COLOR);
            cell.indicatorColor = parseColor(optStringParam(LinearScrollCell.KEY_INDICATOR_COLOR),
                LinearScrollCell.DEFAULT_INDICATOR_COLOR);
            if (data.has(LinearScrollCell.KEY_HAS_INDICATOR)) {
                cell.hasIndicator = data.optBoolean(LinearScrollCell.KEY_HAS_INDICATOR);
            }
            if (data.has(LinearScrollCell.KEY_FOOTER_TYPE)) {
                cell.footerType = data.optString(LinearScrollCell.KEY_FOOTER_TYPE);
            }
            cell.bgColor = parseColor(data.optString(Style.KEY_BG_COLOR), Color.TRANSPARENT);
            cell.retainScrollState = data.optBoolean(LinearScrollCell.KEY_RETAIN_SCROLL_STATE, true);
            cell.scrollMarginLeft = Style.parseSize(data.optString(LinearScrollCell.KEY_SCROLL_MARGIN_LEFT), 0);
            cell.scrollMarginRight = Style.parseSize(data.optString(LinearScrollCell.KEY_SCROLL_MARGIN_RIGHT), 0);
        }
    }

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper oldHelper) {
        GridLayoutHelper helper = new GridLayoutHelper(1);
        helper.setItemCount(getCells().size());
        if (style != null && !Float.isNaN(style.aspectRatio)) {
            helper.setAspectRatio(style.aspectRatio);
        }
        return helper;
    }

    private int parseColor(String color, int defaultColor) {
        if (TextUtils.isEmpty(color)) {
            return defaultColor;
        }
        try {
            return Color.parseColor(color);
        } catch (Exception e) {
            return defaultColor;
        }
    }
}
