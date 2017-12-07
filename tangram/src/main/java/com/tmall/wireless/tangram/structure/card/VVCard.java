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