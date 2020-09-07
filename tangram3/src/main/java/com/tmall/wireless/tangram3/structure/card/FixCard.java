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

package com.tmall.wireless.tangram3.structure.card;

import android.support.annotation.Nullable;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.FixLayoutHelper;
import com.tmall.wireless.tangram3.dataparser.concrete.Card;
import com.tmall.wireless.tangram3.dataparser.concrete.Style;
import com.tmall.wireless.tangram3.util.Utils;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

import static com.alibaba.android.vlayout.layout.FixLayoutHelper.BOTTOM_LEFT;
import static com.alibaba.android.vlayout.layout.FixLayoutHelper.BOTTOM_RIGHT;
import static com.alibaba.android.vlayout.layout.FixLayoutHelper.TOP_LEFT;
import static com.alibaba.android.vlayout.layout.FixLayoutHelper.TOP_RIGHT;
import static com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper.SHOW_ALWAYS;
import static com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper.SHOW_ON_ENTER;
import static com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper.SHOW_ON_LEAVE;

/**
 * Created by villadora on 15/8/24.
 */
public class FixCard extends Card {

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper oldHelper) {
        FixLayoutHelper fixHelper;
        if (oldHelper instanceof FixLayoutHelper) {
            fixHelper = (FixLayoutHelper) oldHelper;
        } else {
            fixHelper = new FixLayoutHelper(0, 0);
        }


        // reset value
        fixHelper.setSketchMeasure(false);
        fixHelper.setItemCount(mCells.size());

        if (style instanceof FixStyle) {
            FixStyle fixStyle = (FixStyle) style;
            fixHelper.setAlignType(fixStyle.alignType);
            fixHelper.setX(fixStyle.x);
            fixHelper.setY(fixStyle.y);
        } else {
            // reset default value
            fixHelper.setAlignType(TOP_LEFT);
            fixHelper.setX(0);
            fixHelper.setY(0);
        }

        fixHelper.setMargin(style.margin[Style.MARGIN_LEFT_INDEX], style.margin[Style.MARGIN_TOP_INDEX],
            style.margin[Style.MARGIN_RIGHT_INDEX], style.margin[Style.MARGIN_BOTTOM_INDEX]);
        fixHelper.setPadding(style.padding[Style.MARGIN_LEFT_INDEX], style.padding[Style.MARGIN_TOP_INDEX],
            style.padding[Style.MARGIN_RIGHT_INDEX], style.padding[Style.MARGIN_BOTTOM_INDEX]);
        return fixHelper;
    }


    public static class FixStyle extends Style {

        public int alignType = TOP_LEFT;
        public int showType = SHOW_ALWAYS;

        public boolean sketchMeasure = true;
        public int x = 0;
        public int y = 0;
    }

}

