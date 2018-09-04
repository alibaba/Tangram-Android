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

package com.tmall.wireless.tangram;

import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;

import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

/**
 * Created by mikeafc on 16/4/26.
 */
public class MVResolver {

    public static final String KEY_TYPE = "type";

    public static final String KEY_STYLE = "style";

    public static final String KEY_BIZ_ID = "bizId";

    public static final String KEY_ID = "id";

    public static final String KEY_TYPE_KEY = "typeKey";

    public static final String KEY_TYPE_REUSEID = "reuseId";

    public static final String KEY_INDEX = "index";

    public static final String KEY_POSITION = "position";

    private ArrayMap<String, Class<? extends View>> typeViewMap = new ArrayMap<>(64);

    private ArrayMap<String, Class<? extends BaseCell>> typeCellMap = new ArrayMap(64);

    private ArrayMap<String, Card> idCardMap = new ArrayMap<>();

    private ArrayMap<BaseCell, View> mvMap = new ArrayMap<>(128);

    private ArrayMap<View, BaseCell> vmMap = new ArrayMap<>(128);

    private ServiceManager mServiceManager;

    public void setServiceManager(ServiceManager serviceManager) {
        mServiceManager = serviceManager;
    }

    public void register(String type, Class<? extends View> viewClazz) {
        typeViewMap.put(type, viewClazz);
    }

    public void registerCompatible(String type, Class<? extends BaseCell> cellClazz) {
        typeCellMap.put(type, cellClazz);
    }

    public boolean isCompatibleType(String type) {
        return typeCellMap.get(type) != null;
    }

    public Class<? extends BaseCell> getCellClass(String type) {
        return typeCellMap.get(type);
    }

    public void register(BaseCell cell, View view) {
        mvMap.put(cell, view);
        vmMap.put(view, cell);
    }

    public void setCards(List<Card> list) {
        synchronized (idCardMap) {
            for (Card card : list) {
                if (!TextUtils.isEmpty(card.id)) {
                    idCardMap.put(card.id, card);
                }
            }
        }
    }

    public Card findCardById(String id) {
        synchronized (idCardMap) {
            return idCardMap.get(id);
        }
    }

    public void reset() {
        mvMap.clear();
        vmMap.clear();
    }

    public View getView(BaseCell cell) {
        return mvMap.get(cell);
    }

    @Deprecated
    public View getView(String uniqueId) {
        return null;
    }

    public BaseCell getCell(View view) {
        return vmMap.get(view);
    }

    public Class<? extends View> getViewClass(String type) {
        return typeViewMap.get(type);
    }

    protected void parseCell(MVHelper resolver, BaseCell cell, JSONObject json) {
        if (json != null) {
            cell.extras = json;
            cell.id = json.optString(KEY_BIZ_ID);
            if (TextUtils.isEmpty(cell.id) && json.has(KEY_ID)) {
                cell.id = json.optString(KEY_ID);
            }
            cell.type = json.optInt(KEY_TYPE);
            cell.stringType = json.optString(KEY_TYPE);
            cell.typeKey = json.optString(KEY_TYPE_KEY);
            String reuseId = json.optString(KEY_TYPE_REUSEID);
            if (!TextUtils.isEmpty(reuseId)) {
                cell.typeKey = reuseId;
            }
			cell.position = json.optInt(KEY_POSITION, -1);
            parseBizParams(cell, json);
            cell.parseWith(json);
            cell.parseWith(json, resolver);
            JSONObject styleJson = json.optJSONObject(KEY_STYLE);
            parseStyle(cell, styleJson);
            parseBizParams(cell, styleJson);
        } else {
            cell.extras = new JSONObject();
        }

    }

    private void parseBizParams(BaseCell cell, JSONObject json) {
        if (json == null) {
            return;
        }

        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            cell.addBizParam(key, json.opt(key));
        }
    }

    protected void parseStyle(BaseCell cell, @Nullable JSONObject json) {
        if (!Utils.isCard(cell.extras)) {
            cell.style = new Style();
            if (json != null) {
                cell.style.parseWith(json);
                cell.parseStyle(json);
            }
        }
    }


}
