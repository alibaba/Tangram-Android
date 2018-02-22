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

import android.support.annotation.Nullable;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.StaggeredGridLayoutHelper;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.Style;

import org.json.JSONObject;

/**
 * Created by villadora on 15/11/8.
 */
public class StaggeredCard extends Card {


    @Override
    public void parseStyle(@Nullable JSONObject data) {
        style = new StaggeredStyle();
        style.parseWith(data);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (style instanceof StaggeredStyle && ((StaggeredStyle) style).column > 0);
    }

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper oldHelper) {
        StaggeredGridLayoutHelper helper = null;
        if (oldHelper instanceof StaggeredGridLayoutHelper) {
            helper = (StaggeredGridLayoutHelper) oldHelper;
        } else {
            helper = new StaggeredGridLayoutHelper();
        }

        if (style instanceof StaggeredStyle) {
            StaggeredStyle sStyle = (StaggeredStyle) style;
            helper.setLane(sStyle.column);
            helper.setItemCount(mCells.size());
            helper.setVGap(sStyle.vGap);
            helper.setHGap(sStyle.hGap);
        }

        helper.setMargin(style.margin[Style.MARGIN_LEFT_INDEX], style.margin[Style.MARGIN_TOP_INDEX],
                style.margin[Style.MARGIN_RIGHT_INDEX], style.margin[Style.MARGIN_BOTTOM_INDEX]);
        helper.setPadding(style.padding[Style.MARGIN_LEFT_INDEX], style.padding[Style.MARGIN_TOP_INDEX],
                style.padding[Style.MARGIN_RIGHT_INDEX], style.padding[Style.MARGIN_BOTTOM_INDEX]);

        return helper;
    }


    static class StaggeredStyle extends Style {
        public static final String KEY_COLUMN = "column";

        public static final String KEY_GAP = "gap";

        public static final String KEY_H_GAP = "hGap";
        public static final String KEY_V_GAP = "vGap";

        public int vGap = 0;
        public int hGap = 0;

        public int column = 2;

        // public float[] cols;

        @Override
        public void parseWith(JSONObject data) {
            super.parseWith(data);

            if (data != null) {

                column = data.optInt(KEY_COLUMN, 2);

                vGap = hGap = Style.parseSize(data.optString(KEY_GAP), 0);

                hGap = Style.parseSize(data.optString(KEY_H_GAP), 0);
                vGap = Style.parseSize(data.optString(KEY_V_GAP), 0);
            }
        }
    }
}
