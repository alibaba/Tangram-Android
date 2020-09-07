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

package com.tmall.wireless.tangram3.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.tmall.wireless.tangram3.TangramBuilder;

/**
 * Created by villadora on 15/10/16.
 */
public class LogUtils {

    private static final String GLOBAL_TAG = "Tangram";

    private static LogDelegate sDefaultLogDelegate = new DefaultLogDelegate();

    public static void setLogDelegate(@NonNull LogDelegate delegate) {
        if (delegate == null)
            throw new RuntimeException("LogDelegate should not be null");

        sDefaultLogDelegate = delegate;
    }

    private static String globalTag(String tag) {
        return GLOBAL_TAG + "[" + tag + "]";
    }


    public static void d(String tag, String msg) {
        if (TangramBuilder.isPrintLog())
            sDefaultLogDelegate.d(globalTag(tag), msg);
    }

    public static void e(String tag, String msg) {
        sDefaultLogDelegate.e(globalTag(tag), msg);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        sDefaultLogDelegate.e(globalTag(tag), msg, throwable);
    }

    public static void i(String tag, String msg) {
        if (TangramBuilder.isPrintLog())
            sDefaultLogDelegate.i(globalTag(tag), msg);
    }

    public static void v(String tag, String msg) {
        if (TangramBuilder.isPrintLog())
            sDefaultLogDelegate.v(globalTag(tag), msg);
    }

    public static void w(String tag, String msg) {
        if (TangramBuilder.isPrintLog())
            sDefaultLogDelegate.w(globalTag(tag), msg);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        if (TangramBuilder.isPrintLog())
            sDefaultLogDelegate.w(globalTag(tag), msg, throwable);
    }


    static class DefaultLogDelegate implements LogDelegate {

        @Override
        public void d(String tag, String msg) {
            Log.d(tag, msg);
        }

        @Override
        public void e(String tag, String msg) {
            Log.e(tag, msg);
        }

        @Override
        public void e(String tag, String msg, Throwable throwable) {
            Log.e(tag, msg, throwable);
        }

        @Override
        public void i(String tag, String msg) {
            Log.i(tag, msg);
        }

        @Override
        public void v(String tag, String msg) {
            Log.v(tag, msg);
        }

        @Override
        public void w(String tag, String msg) {
            Log.w(tag, msg);
        }

        @Override
        public void w(String tag, String msg, Throwable throwable) {
            Log.w(tag, msg, throwable);
        }
    }


    interface LogDelegate {

        void d(String tag, String msg);

        void e(String tag, String msg);

        void e(String tag, String msg, Throwable throwable);

        void i(String tag, String msg);

        void v(String tag, String msg);

        void w(String tag, String msg);

        void w(String tag, String msg, Throwable throwable);
    }

}
