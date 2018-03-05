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

package com.tmall.wireless.tangram.support;

import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;

import com.tmall.wireless.tangram.dataparser.concrete.Cell;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.util.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * ClickSupport which provide a group of methods to handle click events on ComponentViews
 * <p>
 * Usage:
 * <p>
 * <pre>
 *     SimpleClickSupport support = serviceManager.getService(SimpleClickSupport.class)
 *     support.onClick(targetView, thisCell, eventType, [params]);
 * </pre>
 * <p>
 * Which make all the click handlers registered in one place, and decouple the business logic from ComponentViews
 */
public abstract class SimpleClickSupport {
    private static final String TAG = "SimpleClickSupport";

    private static final String ON_CLICK_METHOD_NAME = "onClick";
    private static final String ON_CLICK_METHOD_PREFIX = "on";
    private static final String ON_CLICK_METHOD_POSTFIX = "Click";

    /**
     * Ignore bridge or synthetic methods that added by compiler
     */
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;

    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    private final Map<Class<?>, OnClickMethod> mOnClickMethods = new ArrayMap<>();

    private boolean optimizedMode;

    public SimpleClickSupport() {
    }

    /**
     * enable opt mode, users route click handler by themselves, otherwise click handler is routed by inner and invoked by reflection
     * @param optimizedMode true to enable opt mode
     */
    public void setOptimizedMode(boolean optimizedMode) {
        this.optimizedMode = optimizedMode;
    }

    private void findClickMethods(Method[] methods) {
        for (Method method : methods) {
            String methodName = method.getName();
            if (isValidMethodName(methodName)) {
                int modifiers = method.getModifiers();
                if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 3 || parameterTypes.length == 4) {
                        Class<?> viewType = parameterTypes[0];
                        Class<?> cellType = parameterTypes[1];
                        Class<?> clickIntType = parameterTypes[2];
                        if (View.class.isAssignableFrom(viewType)
                                && BaseCell.class.isAssignableFrom(cellType)
                                && (clickIntType.equals(int.class) || clickIntType.equals(Integer.class))) {
                            if (parameterTypes.length == 4) {
                                Class<?> clickParamsType = parameterTypes[3];
                                if (Map.class.isAssignableFrom(clickParamsType)) {
                                    mOnClickMethods.put(viewType, new OnClickMethod(4, method));
                                }
                            } else {
                                mOnClickMethods.put(viewType, new OnClickMethod(3, method));
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isValidMethodName(String methodName) {
        return !methodName.equals(ON_CLICK_METHOD_NAME) && methodName.startsWith(ON_CLICK_METHOD_NAME) ||
            (methodName.startsWith(ON_CLICK_METHOD_PREFIX) && methodName.endsWith(ON_CLICK_METHOD_POSTFIX));
    }

    /**
     * Handler click event on item
     *
     * @param targetView the view that trigger the click event, not the view respond the cell!
     * @param cell       the corresponding cell
     * @param eventType       click event type, defined by developer.
     */
    public void onClick(View targetView, BaseCell cell, int eventType) {
        if (cell instanceof Cell) {
            onClick(targetView, (Cell) cell, eventType);
        } else {
            onClick(targetView, cell, eventType, null);
        }
    }

    public void onClick(View targetView, Cell cell, int eventType) {
        onClick(targetView, cell, eventType, null);
    }

    public void onClick(View targetView, BaseCell cell, int eventType, Map<String, Object> params) {
        if (optimizedMode) {
            defaultClick(targetView, cell, eventType);
        } else {
            if (mOnClickMethods.isEmpty()) {
                findClickMethods(this.getClass().getMethods());
            }
            List<Class<?>> classes = lookupCellTypes(targetView.getClass());
            for (Class clz : classes) {
                if (clz.equals(View.class)) {
                    continue;
                }
                if (mOnClickMethods.containsKey(clz)) {
                    OnClickMethod onClickMethod = mOnClickMethods.get(clz);
                    try {
                        if (onClickMethod.paramLength == 3) {
                            onClickMethod.method.invoke(this, targetView, cell, eventType);
                            return;
                        } else if (onClickMethod.paramLength == 4) {
                            onClickMethod.method.invoke(this, targetView, cell, eventType, params);
                            return;
                        }
                    } catch (Exception e) {
                        LogUtils.e(TAG, "Invoke onClick method error: " + Log.getStackTraceString(e), e);
                    }
                }
            }
            defaultClick(targetView, cell, eventType);
        }
    }

    public void defaultClick(View targetView, BaseCell cell, int eventType) {

    }

    private static final Map<Class<?>, List<Class<?>>> cellTypesCache = new ConcurrentHashMap<>();

    private List<Class<?>> lookupCellTypes(Class<?> cellClass) {
        List<Class<?>> eventTypes = cellTypesCache.get(cellClass);
        if (eventTypes == null) {
            eventTypes = new ArrayList<>();
            Class<?> clazz = cellClass;
            while (clazz != null && !clazz.equals(BaseCell.class)) {
                eventTypes.add(clazz);
                clazz = clazz.getSuperclass();
            }
            cellTypesCache.put(cellClass, eventTypes);
        }
        return eventTypes;
    }

    static class OnClickMethod {
        int paramLength;
        Method method;

        public OnClickMethod(int paramLength, Method method) {
            this.paramLength = paramLength;
            this.method = method;
        }
    }

}