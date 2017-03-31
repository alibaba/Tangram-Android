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

import android.support.annotation.Nullable;

import com.tmall.wireless.tangram.dataparser.concrete.Style;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by huifeng.hxl on 2015/1/15.
 */
public class ColumnStyle extends Style {

    public static final String KEY_ROWS = "rows";

    @Nullable
    public float[] cols;

    @Nullable
    public float[] rows;

    @Override
    public void parseWith(JSONObject data) {
        super.parseWith(data);
        if (data != null) {
            JSONArray jsonCols = data.optJSONArray(KEY_COLS);
            if (jsonCols != null) {
                cols = new float[jsonCols.length()];
                for (int i = 0; i < cols.length; i++) {
                    cols[i] = (float) jsonCols.optDouble(i, 0);
                }
            } else {
                cols = new float[0];
            }

            JSONArray jsonRows = data.optJSONArray(KEY_ROWS);
            if (jsonRows != null) {
                rows = new float[jsonRows.length()];
                for (int i = 0; i < rows.length; i++) {
                    rows[i] = (float) jsonRows.optDouble(i, 0);
                }
            } else {
                rows = new float[0];
            }
        }
    }

}
