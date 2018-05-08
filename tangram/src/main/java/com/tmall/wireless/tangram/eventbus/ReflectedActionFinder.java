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

package com.tmall.wireless.tangram.eventbus;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by longerian on 16/4/29.
 *
 * Helper to find a callback method of subscriber.
 */
public class ReflectedActionFinder {

    private static final Map<Object, Map<String, Method>> methodCache = new ConcurrentHashMap<>();

    public static void clear() {
        methodCache.clear();
    }

    public static Method findMethodByName(@NonNull String name, @NonNull Object subscriber) {
        if (TextUtils.isEmpty(name)) {
            name = "execute";
        }
        Map<String, Method> methodPair = methodCache.get(subscriber);
        if (methodPair == null) {
            methodPair = new HashMap<>();
            methodCache.put(subscriber, methodPair);
        }
        Method method = methodPair.get(name);
        if (method == null) {
            Class<?> subscriberClass = subscriber.getClass();
            try {
                Class<?> clazz = subscriberClass;
                while (clazz != null && !clazz.equals(Object.class)) {
                    method = subscriberClass.getMethod(name, Event.class);
                    if (method != null) {
                        methodPair.put(name, method);
                        break;
                    }
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return method;
    }

}
