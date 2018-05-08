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

import android.support.annotation.NonNull;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import org.json.JSONObject;

/**
 * Created by longerian on 17/4/10.
 *
 * @author longerian
 * @date 2017/04/10
 */

public class WrapCellCard extends GridCard {

    public WrapCellCard() {
        super(1);
    }

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        maxChildren = 1;
        id = data.optString(KEY_ID, id == null ? "" : id);
        this.type = TangramBuilder.TYPE_SINGLE_COLUMN;
        this.stringType = TangramBuilder.TYPE_CONTAINER_1C_FLOW;
        createCell(this, resolver, data, serviceManager, true);
        //do not need parse style, leave style empty
        this.extras.remove(KEY_STYLE);
        style = new Style();
    }
}