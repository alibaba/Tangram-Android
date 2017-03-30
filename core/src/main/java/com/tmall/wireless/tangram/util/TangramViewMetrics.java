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

package com.tmall.wireless.tangram.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

/**
 * util class to store device info
 */
public final class TangramViewMetrics {

    private static float mDensity = -1f;

    private static int mScreenWidth;

    private static int mScreenHeight;

    public static void initWith(@NonNull final Context context) {

        final Resources resources = context.getResources();
        final DisplayMetrics dm = resources.getDisplayMetrics();
        mDensity = dm.density;

        final Configuration configuration = resources.getConfiguration();
        mScreenWidth = configuration.orientation == Configuration.ORIENTATION_PORTRAIT ? dm.widthPixels : dm.heightPixels;
        mScreenHeight = configuration.orientation == Configuration.ORIENTATION_PORTRAIT ? dm.heightPixels : dm.widthPixels;
    }

    public static int dp2px(float dp) {
        return (int) (mDensity * dp);
    }

    public static int screenWidth() {
        return mScreenWidth;
    }

    public static int screenHeight() {
        return mScreenHeight;
    }

    public static float screenDensity() {
        return mDensity;
    }
}

