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

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.view.ITangramViewLifeCycle;
import com.tmall.wireless.tangram.util.BDE;
import com.tmall.wireless.tangram.util.LifeCycleProviderImpl;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class SingleImageView extends LinearLayout implements ITangramViewLifeCycle {

    public ImageView icon;

    public TextView titleTextView;

    private Context context;

    private final int DEFAULT_ICON_SIZE = Style.dp2px(100);

    public SingleImageView(Context context) {
        this(context, null);
    }

    public SingleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SingleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initUI(context, DEFAULT_ICON_SIZE);
    }

    private void initUI(Context context, int size){
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setBackgroundColor(Color.WHITE);

        icon = new ImageView(context);
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);

        LayoutParams iconLp = new LayoutParams(size, size);
        iconLp.topMargin = Style.dp2px(8);
        addView(icon, iconLp);

        titleTextView = new TextView(context);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        LayoutParams titleLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.topMargin = Style.dp2px(4.0);
        addView(titleTextView, titleLp);

        Space space = new Space(context);
        LayoutParams spaceLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Style.dp2px(8));
        addView(space, spaceLp);
    }


    @Override
    public void cellInited(BaseCell cell) {
        setOnClickListener(cell);
    }

    @Override
    public void postBindView(BaseCell cell) {
        if (cell.serviceManager.supportRx()) {
            LifeCycleProviderImpl<BDE> lifeCycleProvider = cell.getLifeCycleProvider();
            Observable.just(cell).map(new Function<BaseCell, String>() {
                @Override
                public String apply(BaseCell cell) throws Exception {
                    Thread.sleep(500L);
                    int pos = cell.pos;
                    return cell.id + " pos: " + pos + " " + cell.parent.getClass().getSimpleName() + " " + cell
                        .optStringParam("title");
                }
            }).subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(lifeCycleProvider.<String>bindUntil(BDE.UNBIND))
            .subscribe(new Consumer<String>() {
                @Override
                public void accept(String s) throws Exception {
                    titleTextView.setText(s);
                }
            });

            Observable.just(cell).map(new Function<BaseCell, Integer>() {
                @Override
                public Integer apply(BaseCell cell) throws Exception {
                    Thread.sleep(500L);
                    int pos = cell.pos;
                    if (pos > 57) {
                        return 0x66cccf00 + (pos - 50) * 128;
                    } else if (pos % 2 == 0) {
                         return 0xaaaaff55;
                    } else {
                        return 0xcceeeeee;
                    }
                }
            }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(lifeCycleProvider.<Integer>bindUntil(BDE.UNBIND))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer s) throws Exception {
                        icon.setBackgroundColor(s);
                    }
                });
        } else {
            int pos = cell.pos;
            titleTextView.setText(cell.id + " pos: " + pos + " " + cell.parent.getClass().getSimpleName() + " " + cell
                .optStringParam("title"));
            if (pos > 57) {
                icon.setBackgroundColor(0x66cccf00 + (pos - 50) * 128);
            } else if (pos % 2 == 0) {
                icon.setBackgroundColor(0xaaaaff55);
            } else {
                icon.setBackgroundColor(0xcceeeeee);
            }
        }
    }

    @Override
    public void postUnBindView(BaseCell cell) {

    }

    public static int parseColor(String colorStr, String defaultColor) {
        try {
            return Color.parseColor(colorStr);
        } catch (Exception e) {
            return Color.parseColor(defaultColor);
        }
    }

    public static boolean isEmpty(String string){
        return string == null || string.isEmpty() || "null".equalsIgnoreCase(string);
    }

}
