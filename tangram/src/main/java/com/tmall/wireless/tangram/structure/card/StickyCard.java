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
import com.alibaba.android.vlayout.layout.StickyLayoutHelper;
import com.tmall.wireless.tangram.dataparser.concrete.Style;

import org.json.JSONObject;

/**
 * Created by villadora on 15/8/26.
 */
public class StickyCard extends OneItemCard {

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper oldHelper) {
        StickyLayoutHelper helper = null;
        if (oldHelper instanceof StickyLayoutHelper) {
            helper = (StickyLayoutHelper) oldHelper;
        } else {
            helper = new StickyLayoutHelper(true);
        }

        if (style != null)
            if (!Float.isNaN(style.aspectRatio)) {
                helper.setAspectRatio(style.aspectRatio);
            }

        if (style instanceof StickyStyle) {
            helper.setStickyStart(((StickyStyle) style).stickyStart);
            helper.setMargin(style.margin[Style.MARGIN_LEFT_INDEX], style.margin[Style.MARGIN_TOP_INDEX],
                    style.margin[Style.MARGIN_RIGHT_INDEX], style.margin[Style.MARGIN_BOTTOM_INDEX]);
            helper.setPadding(style.padding[Style.MARGIN_LEFT_INDEX], style.padding[Style.MARGIN_TOP_INDEX],
                    style.padding[Style.MARGIN_RIGHT_INDEX], style.padding[Style.MARGIN_BOTTOM_INDEX]);
        } else {
            helper.setStickyStart(true);
        }




        return helper;
    }

    @Override
    public void parseStyle(JSONObject data) {
        this.style = new StickyStyle(true);
        if (data != null)
            style.parseWith(data);
    }

    public static class StickyStyle extends Style {

        public static final String KEY_STICKY = "sticky";

        public static final String STICKY_START = "start";
        public static final String STICKY_END = "end";

        public boolean stickyStart = true;


        public StickyStyle(boolean defaultSticky) {
            this.stickyStart = defaultSticky;
        }

        @Override
        public void parseWith(JSONObject data) {
            super.parseWith(data);
            if (data != null) {
                String sticky = data.optString(KEY_STICKY, stickyStart ? STICKY_START : STICKY_END);
                this.stickyStart = STICKY_START.equalsIgnoreCase(sticky);
            }
        }
    }
}
