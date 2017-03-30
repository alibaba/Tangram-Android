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

package com.tmall.wireless.tangram.structure.entitycard;

import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.card.BannerCard;
import com.tmall.wireless.tangram.structure.cell.BannerCell;
import com.tmall.wireless.tangram.util.LogUtils;
import com.tmall.wireless.tangram.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by longerian on 16/8/22.
 */

public class BannerEntityCard extends BannerCell {

    /** ----parse for inline card start ----*/

    public int cardType;

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        id = data.optString(Card.KEY_ID, id == null ? "" : id);
        this.cardType = data.optInt(Card.KEY_TYPE);
        // parsing header
        if (Utils.isSupportHeaderFooter(cardType)) {
            JSONObject header = data.optJSONObject(Card.KEY_HEADER);
            parseHeaderCell(resolver, header);
        }

        // parsing body
        JSONArray componentArray = data.optJSONArray(Card.KEY_ITEMS);
        if (componentArray != null) {
            final int cellLength = componentArray.length();
            for (int i = 0; i < cellLength; i++) {
                final JSONObject cellData = componentArray.optJSONObject(i);
                createCell(resolver, cellData, true);
            }
        }
        // parsing footer
        if (Utils.isSupportHeaderFooter(cardType)) {
            JSONObject footer = data.optJSONObject(Card.KEY_FOOTER);
            parseFooterCell(resolver, footer);
        }

        JSONObject styleJson = data.optJSONObject(Card.KEY_STYLE);

        parseStyle(styleJson);
    }

    private BaseCell createCell(@NonNull MVHelper resolver, @NonNull JSONObject cellData, boolean appended) {
        if (cellData != null) {
            final int cellType = cellData.optInt(Card.KEY_TYPE, -1);
            if (resolver != null && resolver.resolver().getViewClass(cellType) != null) {
                BaseCell cell;
                if (resolver.resolver().isCompatibleType(cellType)) {
                    cell = Utils.newInstance(resolver.resolver().getCellClass(cellType));

                    //do not display when newInstance failed
                    if (cell == null)
                        return null;
                } else {
                    cell = new BaseCell(cellType);
                }
                parseCell(resolver, cellData, cell, appended);
                cell.type = cellType; // ensure cell type
                cell.serviceManager = serviceManager; // ensure service manager
                cell.nestedParent = this; //ensure parent
                return cell;
            }
        }
        return null;
    }

    protected void parseHeaderCell(@NonNull MVHelper resolver, @Nullable JSONObject header) {
        mHeader = createCell(resolver, header, false);
        if (mHeader != null) {
            mHeader.pos = 0;
            mHeader.parent = null;
            mHeader.nestedParent = this;
            mHeader.parentId = id;
        }
    }

    protected void parseFooterCell(@NonNull MVHelper resolver, @Nullable JSONObject footer) {
        mFooter = createCell(resolver, footer, false);
        if (mFooter != null) {
            mFooter.pos = mHeader != null ? getCells().size() + 1: getCells().size();
            mFooter.parent = null;
            mFooter.nestedParent = this;
            mFooter.parentId = id;
        }
    }

    protected void parseCell(@NonNull MVHelper resolver, @NonNull JSONObject data, @NonNull final BaseCell cell, boolean appended) {
        resolver.parseCell(resolver, cell, data);
        //noinspection unchecked
        if (appended && !addCellInternal(resolver, cell, false)) {
            if (TangramBuilder.isPrintLog())
                LogUtils.w("BannerCell", "Parse invalid cell with data: " + data.toString());
        }
    }

    public List<BaseCell> getCells() {
        return Collections.unmodifiableList(mCells);
    }

    private boolean addCellInternal(MVHelper MVHelper, BaseCell cell, boolean silent) {
        if (cell != null) {
            cell.parentId = id;
            cell.parent = null;
            cell.nestedParent = this;
            cell.serviceManager = serviceManager;
            if (MVHelper != null) {
                if (MVHelper.isValid(cell, serviceManager)) {
                    cell.pos = mHeader != null ? this.mCells.size() + 1 : this.mCells.size();
                    if (!silent && mIsActivated) {
                        // do cell added
                        cell.added();
                    }

                    this.mCells.add(cell);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void parseStyle(@Nullable JSONObject data) {
        style = new Style();
        style.parseWith(data);
        setRatio(style.aspectRatio);
        if (style.bgColor == 0) {
            setBgColor(Color.WHITE);
        } else {
            setBgColor(style.bgColor);
        }
        margin = style.margin;
        if (data != null) {
            setIndicatorRadius(Style.dp2px(data.optDouble(BannerCard.ATTR_INDICATOR_RADIUS)));
            setIndicatorColor(Style.parseColor(data.optString(BannerCard.ATTR_INDICATOR_COLOR, "#00000000")));
            setIndicatorDefaultColor(Style.parseColor(data.optString(BannerCard.ATTR_INDICATOR_DEFAULT_INDICATOR_COLOR, "#00000000")));
            setAutoScrollInternal(data.optInt(BannerCard.ATTR_AUTOSCROLL));
            setInfinite(data.optBoolean(BannerCard.ATTR_INFINITE));
            setInfiniteMinCount(data.optInt(BannerCard.ATTR_INFINITE_MIN_COUNT));
            setIndicatorFocus(data.optString(BannerCard.ATTR_INDICATOR_FOCUS));
            setIndicatorNor(data.optString(BannerCard.ATTR_INDICATOR_NORMAL));
            setIndicatorGravity(data.optString(BannerCard.ATTR_INDICATOR_GRA));
            setIndicatorPos(data.optString(BannerCard.ATTR_INDICATOR_POS));
            setIndicatorGap(Style.dp2px(data.optInt(BannerCard.ATTR_INDICATOR_GAP)));
            setIndicatorMargin(Style.dp2px(data.optInt(BannerCard.ATTR_INDICATOR_MARGIN)));
            setIndicatorHeight(Style.dp2px(data.optInt(BannerCard.ATTR_INDICATOR_HEIGHT)));
            setPageWidth(data.optDouble(BannerCard.ATTR_PAGE_WIDTH));
            sethGap(Style.dp2px(data.optInt(BannerCard.ATTR_HGAP)));
            itemMargin[0] = Style.dp2px(data.optInt(BannerCard.ATTR_ITEM_MARGIN_LEFT));
            itemMargin[1] = Style.dp2px(data.optInt(BannerCard.ATTR_ITEM_MARGIN_RIGHT));
        }
    }
    /** ----parse for inline card end ----*/

}
