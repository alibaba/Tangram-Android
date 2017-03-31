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

package com.tmall.wireless.tangram.structure.style;

import com.tmall.wireless.tangram.MVResolver;
import com.tmall.wireless.tangram.dataparser.concrete.Style;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by villadora on 15/12/3.
 */
public class DelegateStyle extends Style {

    private static final String KEY_MIXED_LAYOUTS = "mixedLayouts";

    public List<CardInfo> cardInfos = new LinkedList<>();

    @Override
    public void parseWith(JSONObject data) {
        super.parseWith(data);
        if (data != null) {
            JSONArray layouts = data.optJSONArray(KEY_MIXED_LAYOUTS);
            if (layouts != null && layouts.length() > 0) {
                for (int i = 0, length = layouts.length(); i < length; i++) {
                    JSONObject l = layouts.optJSONObject(i);
                    if (l != null) {
                        int type = l.optInt(MVResolver.KEY_TYPE, -1);
                        if (type < 0) continue;

                        CardInfo info = new CardInfo();
                        info.type = type;
                        info.itemCount = l.optInt("count", 0);
                        info.data = l;
                        cardInfos.add(info);
                    }
                }
            }
        }
    }


    public static final class CardInfo {
        public int type;
        public int itemCount;
        public JSONObject data;
    }
}
