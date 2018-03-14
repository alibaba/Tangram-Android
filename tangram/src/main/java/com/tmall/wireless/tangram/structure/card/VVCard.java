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

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by longerian on 17/4/2.
 *
 * @author longerian
 * @date 2017/04/02
 */

public class VVCard extends OneItemCard {

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        maxChildren = 1;
        this.extras = data;
        this.stringType = data.optString(KEY_TYPE);
        id = data.optString(KEY_ID, id == null ? "" : id);
        loadMore = data.optInt(KEY_LOAD_TYPE, 0) == LOAD_TYPE_LOADMORE;
        //you should alway assign hasMore to indicate there's more data explicitly
        if (data.has(KEY_HAS_MORE)) {
            hasMore = data.optBoolean(KEY_HAS_MORE);
        } else {
            if (data.has(KEY_LOAD_TYPE)) {
                hasMore = data.optInt(KEY_LOAD_TYPE) == LOAD_TYPE_LOADMORE;
            }
        }
        load = data.optString(KEY_API_LOAD, null);
        loadParams = data.optJSONObject(KEY_API_LOAD_PARAMS);
        loaded = data.optBoolean(KEY_LOADED, false);
        createCell(resolver, extras, true);
        //do not need parse style, leave style empty
        this.extras.remove(KEY_STYLE);
        style = new Style();
    }

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper oldHelper) {
        LinearLayoutHelper helper = new LinearLayoutHelper();
        helper.setItemCount(getCells().size());
        return helper;
    }


}