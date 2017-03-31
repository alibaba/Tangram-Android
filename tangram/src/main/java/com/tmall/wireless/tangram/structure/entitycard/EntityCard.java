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
import com.tmall.wireless.tangram.util.LogUtils;
import com.tmall.wireless.tangram.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by longerian on 16/8/19.
 */

public class EntityCard extends BaseCell {

    private static final String TAG = "EntityCard";

    public int cardType;

    protected List<BaseCell> mCells = new ArrayList<>();

    protected @Nullable BaseCell mHeader;

    protected  @Nullable BaseCell mFooter;

    public EntityCard(int type) {
        super(type);
        inlineCard = true;
    }


    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        id = data.optString(Card.KEY_ID, id == null ? "" : id);
        this.cardType = data.optInt(Card.KEY_TYPE);
        //TODO do not support aysnc load items now
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

    @Override
    public void parseStyle(@Nullable JSONObject data) {
        style = new Style();
        style.parseWith(data);
    }

    protected BaseCell createCell(@NonNull MVHelper resolver, @NonNull JSONObject cellData, boolean appended) {
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

    protected void parseCell(@NonNull MVHelper resolver, @NonNull JSONObject data, @NonNull final BaseCell cell, boolean appended) {
        resolver.parseCell(resolver, cell, data);
        //noinspection unchecked
        if (appended && !addCellInternal(resolver, cell, false)) {
            if (TangramBuilder.isPrintLog())
                LogUtils.w(TAG, "Parse invalid cell with data: " + data.toString());
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
                    if (mFooter != null) {
                        mFooter.pos = cell.pos + 1;
                    }
                    this.mCells.add(cell);
                    return true;
                }
            }
        }

        return false;
    }

    protected void parseHeaderCell(@NonNull MVHelper resolver, @Nullable JSONObject header) {

    }

    protected void parseFooterCell(@NonNull MVHelper resolver, @Nullable JSONObject footer) {

    }

}
