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
import com.alibaba.android.vlayout.layout.StickyLayoutHelper;
import com.tmall.wireless.tangram3.dataparser.concrete.Card;
import com.tmall.wireless.tangram3.dataparser.concrete.Style;

/**
 * Created by villadora on 15/8/26.
 */
public class StickyCard extends Card {

    @Nullable
    @Override
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper oldHelper) {
        StickyLayoutHelper helper = null;
        if (oldHelper instanceof StickyLayoutHelper) {
            helper = (StickyLayoutHelper) oldHelper;
        } else {
            helper = new StickyLayoutHelper(true);
        }

        if (serviceManager != null) {
            helper.setStickyListener(serviceManager.getService(StickyLayoutHelper.StickyListener.class));
            helper.setStackable(serviceManager.getService(StickyLayoutHelper.Stackable.class));
        }

        if (style != null)
            if (!Float.isNaN(style.aspectRatio)) {
                helper.setAspectRatio(style.aspectRatio);
            }

        if (style instanceof StickyStyle) {
            helper.setOffset(((StickyStyle) style).offset);
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

    public static class StickyStyle extends Style {

        public boolean stickyStart;
        public int offset = 0;

        public StickyStyle(boolean defaultSticky) {
            this.stickyStart = defaultSticky;
        }
    }
}
