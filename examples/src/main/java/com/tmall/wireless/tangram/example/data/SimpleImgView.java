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

package com.tmall.wireless.tangram.example.data;

import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.CellRender;
import com.tmall.wireless.tangram.util.ImageUtils;
import com.tmall.wireless.tangram.util.Utils;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by villadora on 15/9/7.
 */
public class SimpleImgView extends RatioImageView {

    public SimpleImgView(Context context) {
        super(context);
        setScaleType(ScaleType.FIT_XY);
    }

    public SimpleImgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.FIT_XY);
    }

    public SimpleImgView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setScaleType(ScaleType.FIT_XY);
    }

    @CellRender
    public void cellInited(BaseCell cell) {
        setOnClickListener(cell);

    }

    @CellRender
    public void postBindView(BaseCell cell) {
        String imgUrl = cell.optStringParam("imgUrl");
        float ratioFromUrl = Utils.getImageRatio(imgUrl);
        setRatio(ratioFromUrl);
        if (cell.style != null) {
            if (!Float.isNaN(cell.style.aspectRatio)) {
                setRatio(cell.style.aspectRatio, RatioImageView.PRIORITY_HIGH);
            }
        }
        ImageUtils.doLoadImageUrl(this, imgUrl);
        setOnClickListener(cell);
    }

    @CellRender
    public void postUnBindView(BaseCell cell) {
    }
}
