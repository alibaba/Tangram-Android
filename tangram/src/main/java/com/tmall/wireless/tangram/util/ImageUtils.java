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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Usually each app has a unique custom ImageView, this util helps to decouple the Tangram framework and app on custom ImageView class.<br />
 * It has two features:<br />
 * 1. construct a new ImageView instance of app's custom ImageView class. see {@link #createImageInstance(Context)}. Of cource, providing a custom ImageView during init phase is necessary.<br />
 * 2. provide an interface {@link #doLoadImageUrl(ImageView, String)} to Tangram framework to load image.<br />
 *
 * Created by villadora on 15/9/7.
 */
public final class ImageUtils {

    private static IInnerImageSetter sImageSetter;

    public static Class<? extends ImageView> sImageClass;

    private static Constructor imageViewConstructor;

    /**
     * create a custom ImageView instance
     * @param context activity context
     * @return an instance
     */
    public static ImageView createImageInstance(Context context) {
        if (sImageClass != null) {
            if (imageViewConstructor == null) {
                try {
                    imageViewConstructor = sImageClass.getConstructor(Context.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            if (imageViewConstructor != null) {
                try {
                    return (ImageView) imageViewConstructor.newInstance(context);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void setImageSetter(@NonNull IInnerImageSetter imageSetter) {
        sImageSetter = imageSetter;
    }

    /**
     * load image using {@link IInnerImageSetter}
     * @param view the imageView instance
     * @param url image url
     * @param <IMAGE> ImageView class type
     */
    public static <IMAGE extends ImageView> void doLoadImageUrl(@NonNull IMAGE view, @Nullable String url) {
        Preconditions.checkState(sImageSetter != null, "ImageSetter must be initialized before calling image load");
        sImageSetter.doLoadImageUrl(view, url);
    }
}
