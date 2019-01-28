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
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ImageView that can set ratio
 */
public class RatioImageView extends ImageView {

    private static final String TAG = "RatioImageView";

    /**
     * Fit ratio by width, which means width has fixed size, height = width /ratio
     */
    public static final int FIX_BY_WIDTH = 0;
    /**
     * Fit ratio by height, which means height has fixed size, width = height * ratio
     */
    public static final int FIX_BY_HEIGHT = 1;

    @IntDef(flag = true, value = {FIX_BY_WIDTH, FIX_BY_HEIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FixBy {
    }

    public static final int PRIORITY_HIGH = 1 << 1;
    public static final int PRIORITY_LOW = 1;

    @IntDef(flag = true, value = {PRIORITY_HIGH, PRIORITY_LOW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RatioPriority {
    }


    /**
     * ratio represents width / height
     */
    private float mRatio = Float.NaN;

    @FixBy
    private int mFixBy = FIX_BY_WIDTH;

    @RatioPriority
    private int mPriority = PRIORITY_LOW;

    public RatioImageView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public void init(Context context, AttributeSet attrs, int defStyle) {

    }

    /**
     * Set ratio of this image
     *
     * @param ratio
     */
    public void setRatio(float ratio) {
        setRatio(ratio, PRIORITY_LOW);
    }

    public void setRatio(float ratio, @RatioPriority int priority) {
        this.mRatio = ratio;
        this.mPriority = priority;
    }

    public float getRatio() {
        return mRatio;
    }

    /**
     * Calculate rect by which dimension, {@link #FIX_BY_WIDTH} or {@link #FIX_BY_HEIGHT}
     *
     * @param byWhat
     */
    public void setFixBy(@FixBy int byWhat) {
        this.mFixBy = byWhat;
    }


    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable != null) {
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            if (drawableHeight > 0 && mPriority == PRIORITY_LOW) {
                setRatio((float) drawableWidth / drawableHeight);
            }
        }
        super.setImageDrawable(drawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // if isNaN, skip
        if (!Float.isNaN(mRatio)) {
            // calculate by width
            if (mFixBy == FIX_BY_WIDTH) {
                int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
                int height = View.MeasureSpec.getSize(heightMeasureSpec);
                // if mode is MeasureSpec.EXACTLY, just use the specified size
                if (heightMode != View.MeasureSpec.EXACTLY) {
                    int measuredWidth = getMeasuredWidth();
                    if (heightMode == View.MeasureSpec.AT_MOST)
                        // if mode is AT_MOST, pick the minimum one
                        height = Math.min(height, (int) (measuredWidth / mRatio + 0.5f));
                    else
                        // if mode is UNSPECIFIED, use the calculated size
                        height = (int) (measuredWidth / mRatio + 0.5f);

                    // set re-calculated size
                    setMeasuredDimension(measuredWidth, height);
                }
            } else if (mFixBy == FIX_BY_HEIGHT) {
                int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
                int width = View.MeasureSpec.getSize(widthMeasureSpec);
                if (widthMode != View.MeasureSpec.EXACTLY) {
                    int measureHeight = getMeasuredHeight();
                    if (widthMode == View.MeasureSpec.AT_MOST)
                        width = Math.min(width, (int) (measureHeight * mRatio + 0.5f));
                    else
                        width = (int) (measureHeight * mRatio + 0.5f);
                    setMeasuredDimension(width, measureHeight);
                }
            }
        }
    }

}
