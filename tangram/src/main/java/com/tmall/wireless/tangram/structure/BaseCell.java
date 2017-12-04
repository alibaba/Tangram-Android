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

package com.tmall.wireless.tangram.structure;

import com.tmall.wireless.tangram.Engine;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.ComponentLifecycle;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.expression.ITangramExprParser;
import com.tmall.wireless.tangram.expression.TangramExpr;
import com.tmall.wireless.tangram.support.SimpleClickSupport;
import com.tmall.wireless.tangram.util.IInnerImageSetter;
import com.tmall.wireless.tangram.util.ImageUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mikeafc on 16/4/25.
 */
public class BaseCell<V extends View> extends ComponentLifecycle implements View.OnClickListener,
        ITangramExprParser {
    private static AtomicLong sIdGen = new AtomicLong();

    public static boolean sIsGenIds = false;

    /**
     * cell's type
     */
    public int type;

    /**
     * indicate whether this cell is a cellized card
     */
    public boolean inlineCard;

    /**
     * parent's id
     */
    @Nullable
    public String parentId;

    /**
     * parent
     */
    public Card parent;

    /**
     * the parent of a cellized card
     */
    public ComponentLifecycle nestedParent;

    /**
     * id of a cell
     */
    @Nullable
    public String id;

    /**
     * the natural position this cell in its parent
     */
    public int pos;

    /**
     * position that assigned from server side
     */
    public int position = -1;

    /**
     * cell's style
     */
    @Nullable
    public Style style;

    /**
     * item type for adapter.<br />
     * by default, the item type is calculated by {@link #type}, which means cell with same type share a recycler pool.
     * if you set a unique typeKey to cell, the item type is calculated by {@link #typeKey}, which measn cells with same typeKey share a recycler pool. This may be dangerous you must ensure the same typeKey must be assigned to the same type of cell.<br />
     * best practice is that if you have 10 cells with same type and need a certain one to a independent recycler pool.
     */
    public String typeKey;

    /**
     * inner use, item id for adapter.
     */
    public final long objectId;

    /**
     * the original json data
     */
    public JSONObject extras = new JSONObject();

    private ArrayMap<String, Object> bizParaMap = new ArrayMap<>(32);

    private ArrayMap<Integer, Integer> innerClickMap = new ArrayMap<>();

    @Nullable
    public ServiceManager serviceManager;

    public boolean mIsExposed = false;

    public BaseCell() {
        objectId = sIsGenIds ? sIdGen.getAndIncrement() : 0;
    }

    public BaseCell(int type) {
        this.type = type;
        objectId = sIsGenIds ? sIdGen.getAndIncrement() : 0;
    }

    public void addBizParam(String key, Object value) {
        bizParaMap.put(key, value);
    }

    public Map<String, Object> getAllBizParams() {
        return bizParaMap;
    }

    @Override
    public void onClick(View v) {
        if (serviceManager != null) {
            SimpleClickSupport service = serviceManager.getService(SimpleClickSupport.class);
            if (service != null) {
                int pos = this.pos;
                if (innerClickMap.containsKey(v)) {
                    pos = innerClickMap.get(v.hashCode()).intValue();
                }
                service.onClick(v, this, pos);
            }
        }
    }

    public void setOnClickListener(View view, int eventType) {
        view.setOnClickListener(this);
        innerClickMap.put(view.hashCode(), Integer.valueOf(eventType));
    }

    public void clearClickListener(View view, int eventType) {
        view.setOnClickListener(null);
        innerClickMap.remove(view.hashCode());
    }

    public final void notifyDataChange() {
        if (serviceManager instanceof Engine) {
            ((Engine) serviceManager).refresh(false);
        }
    }


    public final void doLoadImageUrl(ImageView view, String imgUrl) {
        if (serviceManager != null && serviceManager.getService(IInnerImageSetter.class) != null) {
            serviceManager.getService(IInnerImageSetter.class).doLoadImageUrl(view, imgUrl);
        } else {
            ImageUtils.doLoadImageUrl(view, imgUrl);
        }
    }

    public boolean hasParam(String key) {
        return extras.has(key) ||
                style != null && style.extras != null && style.extras.has(key);
    }

    public Object optParam(String key) {
        if (extras.has(key))
            return extras.opt(key);
        if (style != null && style.extras != null)
            return style.extras.opt(key);
        return null;
    }

    public long optLongParam(String key) {
        if (extras.has(key))
            return extras.optLong(key);
        if (style != null && style.extras != null)
            return style.extras.optLong(key);
        return 0;
    }

    public int optIntParam(String key) {
        if (extras.has(key))
            return extras.optInt(key);
        if (style != null && style.extras != null)
            return style.extras.optInt(key);
        return 0;
    }

    public String optStringParam(String key) {
        if (extras.has(key))
            return extras.optString(key);
        if (style != null && style.extras != null)
            return style.extras.optString(key);
        return "";
    }

    public double optDoubleParam(String key) {
        if (extras.has(key))
            return extras.optDouble(key);
        if (style != null && style.extras != null)
            return style.extras.optDouble(key);
        return Double.NaN;
    }

    public boolean optBoolParam(String key) {
        if (extras.has(key))
            return extras.optBoolean(key);
        return style != null && style.extras != null && style.extras.optBoolean(key);
    }

    public JSONObject optJsonObjectParam(String key) {
        if (extras.has(key))
            return extras.optJSONObject(key);
        if (style != null && style.extras != null)
            return style.extras.optJSONObject(key);
        return null;
    }

    public JSONArray optJsonArrayParam(String key) {
        if (extras.has(key))
            return extras.optJSONArray(key);
        if (style != null && style.extras != null)
            return style.extras.optJSONArray(key);
        return null;
    }

    /***
     * for compatible
     */
    public void parseWith(JSONObject data) {

    }

    /***
     * for compatible
     */
    public void parseStyle(@Nullable JSONObject data) {

    }

    public void parseWith(@NonNull JSONObject data, @NonNull final MVHelper resolver) {

    }

    /***
     * for compatible
     */
    public void bindView(@NonNull V view) {

    }

    /***
     * for compatible
     */
    public void postBindView(@NonNull V view) {

    }

    /***
     * for compatible
     */
    public void unbindView(@NonNull V view) {
        clearClickListener(view, 0);
    }

    /***
     * for compatible
     */
    public boolean isValid() {
        return true;
    }

    @Override
    public Object getValueBy(TangramExpr var) {
        if (var.hasNextFragment()) {
            String next = var.nextFragment();
            return optParam(next);
        } else {
            return extras;
        }
    }
}
