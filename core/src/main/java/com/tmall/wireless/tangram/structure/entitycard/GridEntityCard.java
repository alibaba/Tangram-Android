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
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by longerian on 16/8/19.
 */

public class GridEntityCard extends EntityCard {

    private static final String TAG = "GridEntityCard";

    private int mColumn = 0;

    public GridEntityCard(int type) {
        super(type);
    }

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        super.parseWith(data, resolver);
        this.mColumn = Utils.getCardColumnCount(cardType);
    }

    @Override
    public void parseStyle(@Nullable JSONObject data) {
        style = new GridEntityStyle();
        style.parseWith(data);
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
                Log.w(TAG, Log.getStackTraceString(e), e);
            }
        }
    }


    public static class GridEntityStyle extends Style {
        public static final String KEY_COLUMN = "column";

        public static final String KEY_AUTO_EXPAND = "autoExpand";

        public static final String KEY_IGNORE_EXTRA = "ignoreExtra";

        public static final String KEY_H_GAP = "hGap";
        public static final String KEY_V_GAP = "vGap";

        public int vGap = 0;
        public int hGap = 0;

        public boolean autoExpand = false;

        public int column = 0;

        public float[] cols;

        @Override
        public void parseWith(JSONObject data) {
            super.parseWith(data);

            if (data != null) {
                column = data.optInt(KEY_COLUMN, 0);

                autoExpand = data.optBoolean(KEY_AUTO_EXPAND, false);

                JSONArray jsonCols = data.optJSONArray(KEY_COLS);
                if (jsonCols != null) {
                    cols = new float[jsonCols.length()];
                    for (int i = 0; i < cols.length; i++) {
                        cols[i] = (float) jsonCols.optDouble(i, 0);
                    }
                } else {
                    cols = new float[0];
                }

                hGap = Style.dp2px(data.optDouble(KEY_H_GAP, 0.0f));
                vGap = Style.dp2px(data.optDouble(KEY_V_GAP, 0.0f));
            }
        }
    }

}
