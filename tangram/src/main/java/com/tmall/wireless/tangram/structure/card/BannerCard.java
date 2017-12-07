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

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.MVResolver;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.cell.BannerCell;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by mikeafc on 16/1/12.
 */
public class BannerCard extends Card {
    public static final String ATTR_AUTOSCROLL = "autoScroll";
    public static final String ATTR_SPECIAL_INTERVAL = "specialInterval";
    public static final String ATTR_INFINITE = "infinite";
    public static final String ATTR_INDICATOR_FOCUS = "indicatorImg1";
    public static final String ATTR_INDICATOR_NORMAL = "indicatorImg2";
    public static final String ATTR_INDICATOR_GRA = "indicatorGravity";
    public static final String ATTR_INDICATOR_POS = "indicatorPosition";
    public static final String ATTR_INDICATOR_GAP = "indicatorGap";
    public static final String ATTR_INDICATOR_HEIGHT = "indicatorHeight";
    public static final String ATTR_INDICATOR_MARGIN = "indicatorMargin";
    public static final String ATTR_INFINITE_MIN_COUNT = "infiniteMinCount";
    public static final String ATTR_PAGE_WIDTH = "pageRatio";
    public static final String ATTR_HGAP = "hGap";
    public static final String ATTR_ITEM_MARGIN_LEFT = "scrollMarginLeft";
    public static final String ATTR_ITEM_MARGIN_RIGHT = "scrollMarginRight";
    public static final String ATTR_ITEM_RATIO = "itemRatio";

    public static final String ATTR_INDICATOR_RADIUS = "indicatorRadius";
    public static final String ATTR_INDICATOR_COLOR = "indicatorColor";
    public static final String ATTR_INDICATOR_DEFAULT_INDICATOR_COLOR = "defaultIndicatorColor";

    private BannerCell cell;

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        if (cell == null)
            cell = new BannerCell();

        super.parseWith(data, resolver);
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", TangramBuilder.TYPE_CAROUSEL_CELL);
            obj.put("bizId", id);

            resolver.parseCell(resolver, cell, obj);

            if (!super.getCells().isEmpty()) {
                cell.mCells.addAll(super.getCells());
                for (int i = 0, size = cell.mCells.size(); i < size; i++) {
                    try {
                        BaseCell item = cell.mCells.get(i);
                        item.extras.put(MVResolver.KEY_INDEX, item.pos);
                    } catch (JSONException e) {
                    }
                }
                super.setCells(Collections.<BaseCell>singletonList(cell));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            super.setCells(Collections.<BaseCell>singletonList(cell));
            cell.setData(cells);
        }

        notifyDataChange();
    }

    @Override
    public void parseStyle(@Nullable JSONObject data) {
        super.parseStyle(data);
        if (data == null)
            return;
        cell.setIndicatorRadius(Style.dp2px(data.optDouble(ATTR_INDICATOR_RADIUS)));
        cell.setIndicatorColor(Style.parseColor(data.optString(ATTR_INDICATOR_COLOR, "#00000000")));
        cell.setIndicatorDefaultColor(Style.parseColor(data.optString(ATTR_INDICATOR_DEFAULT_INDICATOR_COLOR, "#00000000")));
        cell.setAutoScrollInternal(data.optInt(ATTR_AUTOSCROLL));
        cell.setSpecialInterval(data.optJSONObject(ATTR_SPECIAL_INTERVAL));
        cell.setInfinite(data.optBoolean(ATTR_INFINITE));
        cell.setInfiniteMinCount(data.optInt(ATTR_INFINITE_MIN_COUNT));
        cell.setIndicatorFocus(data.optString(ATTR_INDICATOR_FOCUS));
        cell.setIndicatorNor(data.optString(ATTR_INDICATOR_NORMAL));
        cell.setIndicatorGravity(data.optString(ATTR_INDICATOR_GRA));
        cell.setIndicatorPos(data.optString(ATTR_INDICATOR_POS));
        cell.setIndicatorGap(Style.dp2px(data.optInt(ATTR_INDICATOR_GAP)));
        cell.setIndicatorMargin(Style.dp2px(data.optInt(ATTR_INDICATOR_MARGIN)));
        cell.setIndicatorHeight(Style.dp2px(data.optInt(ATTR_INDICATOR_HEIGHT)));
        cell.setPageWidth(data.optDouble(ATTR_PAGE_WIDTH));
        cell.sethGap(Style.dp2px(data.optInt(ATTR_HGAP)));
        cell.itemRatio = data.optDouble(BannerCard.ATTR_ITEM_RATIO, Double.NaN);
        cell.itemMargin[0] = Style.dp2px(data.optInt(ATTR_ITEM_MARGIN_LEFT));
        cell.itemMargin[1] = Style.dp2px(data.optInt(ATTR_ITEM_MARGIN_RIGHT));
        if (style != null) {
            cell.setRatio(style.aspectRatio);
            cell.margin = style.margin;
        }
    }

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(LayoutHelper oldHelper) {
        LinearLayoutHelper helper = new LinearLayoutHelper();
        helper.setItemCount(getCells().size());
        return helper;
    }


}
