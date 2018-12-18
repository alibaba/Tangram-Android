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

package com.tmall.wireless.tangram.example.tangram3;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tmall.wireless.tangram.example.R;
import com.tmall.wireless.tangram.example.support.SampleScrollSupport;
import com.tmall.wireless.tangram3.structure.BaseCell;
import com.tmall.wireless.tangram3.structure.view.ITangramViewLifeCycle;

/**
 * Created by villadora on 15/8/24.
 */
public class TestBlockView extends FrameLayout implements ITangramViewLifeCycle, SampleScrollSupport.IScrollListener {
    private TextView textView;
    private BaseCell cell;

    public TestBlockView(Context context) {
        super(context);
        init();
    }

    public TestBlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestBlockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.item, this);
        textView = (TextView) findViewById(R.id.title);
    }

    @Override
    public void cellInited(BaseCell cell) {
        setOnClickListener(cell);
        this.cell = cell;
        if (cell.serviceManager != null) {
            SampleScrollSupport scrollSupport = cell.serviceManager.getService(SampleScrollSupport.class);
            scrollSupport.register(this);
        }
    }

    @Override
    public void postBindView(BaseCell cell) {
        int pos = cell.pos;
        String parent = "";
        if (cell.parent != null) {
            parent = cell.parent.getClass().getSimpleName();
        }
        textView.setText(
                cell.id + " pos: " + pos + " " + parent + " " + cell
                        .optParam("msg"));

        if (pos > 57) {
            textView.setBackgroundColor(0x66cccf00 + (pos - 50) * 128);
        } else if (pos % 2 == 0) {
            textView.setBackgroundColor(0xaaaaff55);
        } else {
            textView.setBackgroundColor(0xcceeeeee);
        }
    }

    @Override
    public void postUnBindView(BaseCell cell) {
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        Log.i("TestView", "onScrollStateChanged: ");
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        Log.i("TestView", "onScrolled: ");
    }
}
