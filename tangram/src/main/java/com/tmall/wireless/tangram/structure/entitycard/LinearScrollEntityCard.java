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

package com.tmall.wireless.tangram.structure.entitycard;

import java.util.Collections;
import java.util.List;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.MVResolver;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.BaseCellBinderResolver;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.cell.LinearScrollCell;
import com.tmall.wireless.tangram.util.LogUtils;
import com.tmall.wireless.tangram.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tmall.wireless.tangram.dataparser.concrete.Style.parseColor;

/**
 * Created by longerian on 2018/2/22.
 *
 * @author longerian
 * @date 2018/02/22
 */

public class LinearScrollEntityCard extends LinearScrollCell {

    public String cardType;

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        this.cells.clear();
        id = data.optString(Card.KEY_ID, id == null ? "" : id);
        this.cardType = data.optString(Card.KEY_TYPE);
        // parsing header
        JSONObject header = data.optJSONObject(Card.KEY_HEADER);
        parseHeaderCell(resolver, header);

        // parsing body
        JSONArray componentArray = data.optJSONArray(Card.KEY_ITEMS);
        if (componentArray != null) {
            final int cellLength = componentArray.length();
            for (int i = 0; i < cellLength; i++) {
                final JSONObject cellData = componentArray.optJSONObject(i);
                BaseCell cell = createCell(resolver, cellData, true);
                try {
                    if (cell != null) {
                        cell.extras.put(MVResolver.KEY_INDEX, cell.pos);
                    }
                } catch (JSONException e) {
                }
            }
        }
        // parsing footer
        JSONObject footer = data.optJSONObject(Card.KEY_FOOTER);
        parseFooterCell(resolver, footer);

        JSONObject styleJson = data.optJSONObject(Card.KEY_STYLE);

        parseStyle(styleJson);
    }

    private BaseCell createCell(@NonNull MVHelper resolver, @NonNull JSONObject cellData, boolean appended) {
        if (cellData != null) {
            BaseCell cell = null;
            String cellType = cellData.optString(Card.KEY_TYPE);
            if ((resolver != null && resolver.resolver().getViewClass(cellType) != null) || Utils.isCard(cellData)) {
                if (resolver.resolver().isCompatibleType(cellType)) {
                    cell = Utils.newInstance(resolver.resolver().getCellClass(cellType));
                    //do not display when newInstance failed
                    if (cell == null) {
                        return null;
                    }
                    cell.serviceManager = serviceManager;
                } else {
                    cell = new BaseCell(cellType);
                    cell.serviceManager = serviceManager;
                    cell.nestedParent = this;
                    cell.parentId = id;
                }
                if (cell != null) {
                    parseCell(resolver, cellData, cell, appended);
                    cell.setStringType(cellType);
                }
                return cell;
            } else {
                //support virtual view at layout
                BaseCellBinderResolver componentBinderResolver = serviceManager.getService(BaseCellBinderResolver.class);
                if (componentBinderResolver.has(cellType)) {
                    cell = new BaseCell(cellType);
                    cell.serviceManager = serviceManager;
                    cell.nestedParent = this;
                    cell.parentId = id;
                    parseCell(resolver, cellData, cell, appended);
                    cell.setStringType(cellType);
                    return cell;
                } else {
                    return null;
                }
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
            try {
                mHeader.extras.put(MVResolver.KEY_INDEX, mHeader.pos);
            } catch (JSONException e) {
            }
        }
    }

    protected void parseFooterCell(@NonNull MVHelper resolver, @Nullable JSONObject footer) {
        mFooter = createCell(resolver, footer, false);
        if (mFooter != null) {
            mFooter.pos = mHeader != null ? getCells().size() + 1 : getCells().size();
            mFooter.parent = null;
            mFooter.nestedParent = this;
            mFooter.parentId = id;
            try {
                mFooter.extras.put(MVResolver.KEY_INDEX, mFooter.pos);
            } catch (JSONException e) {
            }
        }
    }

    protected void parseCell(@NonNull MVHelper resolver, @NonNull JSONObject data, @NonNull final BaseCell cell, boolean appended) {
        resolver.parseCell(resolver, cell, data);
        //noinspection unchecked
        if (appended && !addCellInternal(resolver, cell, false)) {
            if (TangramBuilder.isPrintLog()) {
                LogUtils.w("LinearScrollCell", "Parse invalid cell with data: " + data.toString());
            }
        }
    }

    public List<BaseCell> getCells() {
        return Collections.unmodifiableList(cells);
    }

    private boolean addCellInternal(MVHelper mvHelper, BaseCell cell, boolean silent) {
        if (cell != null) {
            cell.parentId = id;
            cell.parent = null;
            cell.nestedParent = this;
            cell.serviceManager = serviceManager;
            if (mvHelper != null) {
                if (mvHelper.isValid(cell, serviceManager)) {
                    cell.pos = mHeader != null ? this.cells.size() + 1 : this.cells.size();
                    if (!silent && mIsActivated) {
                        // do cell added
                        cell.added();
                    }
                    this.cells.add(cell);
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
        if (data != null) {
            pageWidth = Style.parseSize(optStringParam(LinearScrollCell.KEY_PAGE_WIDTH), 0);
            pageHeight = Style.parseSize(optStringParam(LinearScrollCell.KEY_PAGE_HEIGHT), 0);

            defaultIndicatorColor = parseColor(optStringParam(LinearScrollCell.KEY_DEFAULT_INDICATOR_COLOR),
                LinearScrollCell.DEFAULT_DEFAULT_INDICATOR_COLOR);
            indicatorColor = parseColor(optStringParam(LinearScrollCell.KEY_INDICATOR_COLOR),
                LinearScrollCell.DEFAULT_INDICATOR_COLOR);
            if (data.has(LinearScrollCell.KEY_HAS_INDICATOR)) {
                hasIndicator = data.optBoolean(LinearScrollCell.KEY_HAS_INDICATOR);
            }
            if (data.has(LinearScrollCell.KEY_FOOTER_TYPE)) {
                footerType = data.optString(LinearScrollCell.KEY_FOOTER_TYPE);
            }
            bgColor = parseColor(data.optString(Style.KEY_BG_COLOR), Color.TRANSPARENT);
            retainScrollState = data.optBoolean(LinearScrollCell.KEY_RETAIN_SCROLL_STATE, true);
            scrollMarginLeft = Style.parseSize(data.optString(LinearScrollCell.KEY_SCROLL_MARGIN_LEFT), 0);
            scrollMarginRight = Style.parseSize(data.optString(LinearScrollCell.KEY_SCROLL_MARGIN_RIGHT), 0);
        }
    }

}
