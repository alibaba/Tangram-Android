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

import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.util.LogUtils;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Exposure which provide a group of methods to handle exposure events on ComponentViews and Card
 * <p>
 * Usage:
 * <p>
 * <pre>
 *     ExposureSupport support = serviceManager.getService(ExposureSupport.class)
 *     support.onExposure(targetView, thisCell, mEventType);
 * </pre>
 * <p>
 * Which make all the click handlers registered in one place, and decouple the business logic from ComponentViews
 * <br />
 * Created by villadora on 15/11/11.
 */
public abstract class ExposureSupport {

    private static final String TAG = "ExposureSupport";

    private static final String ON_TRACE_METHOD_NAME = "onTrace";

    private static final String ON_TRACE_METHOD_PREFIX = "on";

    private static final String ON_TRACE_METHOD_POSTFIX = "Trace";

    private static final String ON_EXPOSURE_METHOD_NAME = "onExposure";

    private static final String ON_EXPOSURE_METHOD_PREFIX = "on";

    private static final String ON_EXPOSURE_METHOD_POSTFIX = "Exposure";

    /**
     * Ignore bridge or synthetic methods that added by compiler
     */
    private static final int BRIDGE = 0x40;

    private static final int SYNTHETIC = 0x1000;

    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE
            | SYNTHETIC;

    private final Map<Class<?>, OnTraceMethod> mOnTraceMethods = new ArrayMap<>();
    private final Map<Class<?>, OnTraceMethod> mOnExposureMethods = new ArrayMap<>();

    private boolean optimizedMode;

    public ExposureSupport() {
    }

    private void findTraceMethods(Method[] methods) {
        for (Method method : methods) {
            String methodName = method.getName();
            if (isValidTraceMethodName(methodName)) {
                int modifiers = method.getModifiers();
                if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 3) {
                        Class<?> viewType = parameterTypes[0];
                        Class<?> cellType = parameterTypes[1];
                        Class<?> clickIntType = parameterTypes[2];
                        if (View.class.isAssignableFrom(viewType)
                                && BaseCell.class.isAssignableFrom(cellType)
                                && (clickIntType.equals(int.class) || clickIntType
                                .equals(Integer.class))) {
                            mOnTraceMethods.put(viewType, new OnTraceMethod(3, method));
                        }
                    }
                }
            }
        }
    }

    private boolean isValidTraceMethodName(String methodName) {
        return !methodName.equals(ON_TRACE_METHOD_NAME) && methodName.startsWith(ON_TRACE_METHOD_NAME) ||
            (methodName.startsWith(ON_TRACE_METHOD_PREFIX) && methodName.endsWith(ON_TRACE_METHOD_POSTFIX));
    }

    private void findExposureMethods(Method[] methods) {
        for (Method method : methods) {
            String methodName = method.getName();
            if (isValidExposureMethodName(methodName)) {
                int modifiers = method.getModifiers();
                if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 3) {
                        Class<?> viewType = parameterTypes[0];
                        Class<?> cellType = parameterTypes[1];
                        Class<?> clickIntType = parameterTypes[2];
                        if (View.class.isAssignableFrom(viewType)
                                && BaseCell.class.isAssignableFrom(cellType)
                                && (clickIntType.equals(int.class) || clickIntType
                                .equals(Integer.class))) {
                            mOnExposureMethods.put(viewType, new OnTraceMethod(3, method));
                        }
                    }
                }
            }
        }
    }

    private boolean isValidExposureMethodName(String methodName) {
        return !methodName.equals(ON_EXPOSURE_METHOD_NAME) && methodName.startsWith(ON_EXPOSURE_METHOD_NAME) ||
            (methodName.startsWith(ON_EXPOSURE_METHOD_PREFIX) && methodName.endsWith(ON_EXPOSURE_METHOD_POSTFIX));
    }

    /**
     * enable opt mode, users route exposure handler by themselves, otherwise exposure handler is routed by inner and invoked by reflection
     * @param optimizedMode true to enable opt mode
     */
    public void setOptimizedMode(boolean optimizedMode) {
        this.optimizedMode = optimizedMode;
    }

    /**
     * Handler exposure event on item
     *
     * @param targetView the view that trigger the exposure event, not the view respond the cell!
     * @param cell       the corresponding cell
     * @param type       exposure event type, defined by developer.
     */
    public void onTrace(@NonNull View targetView, @NonNull BaseCell cell, int type) {
        if (optimizedMode) {
            defaultTrace(targetView, cell, type);
        } else {
            if (mOnExposureMethods.isEmpty() || mOnTraceMethods.isEmpty()) {
                Method[] methods = this.getClass().getMethods();
                findTraceMethods(methods);
                findExposureMethods(methods);
            }
            List<Class<?>> classes = lookupCellTypes(targetView.getClass());
            for (Class clz : classes) {
                if (clz.equals(View.class)){
                    continue;
                }
                if (mOnTraceMethods.containsKey(clz)) {
                    OnTraceMethod onTraceMethod = mOnTraceMethods.get(clz);
                    try {
                        if (onTraceMethod.paramLength == 3) {
                            onTraceMethod.method.invoke(this, targetView, cell, type);
                            return;
                        }
                    } catch (Exception e) {
                        LogUtils.e(TAG, "Invoke Trace method error: " + Log.getStackTraceString(e),
                                e);
                    }
                }
            }
            defaultTrace(targetView, cell, type);
        }
    }

    public void defaultTrace(@NonNull View targetView, @NonNull BaseCell cell, int type) {

    }

    public void onExposure(@NonNull View targetView, @NonNull BaseCell cell, int type) {
        if (optimizedMode) {
            defaultExposureCell(targetView, cell, type);
        } else {
            if (mOnExposureMethods.isEmpty() || mOnTraceMethods.isEmpty()) {
                Method[] methods = this.getClass().getMethods();
                findTraceMethods(methods);
                findExposureMethods(methods);
            }
            List<Class<?>> classes = lookupCellTypes(targetView.getClass());
            for (Class clz : classes) {
                if (clz.equals(View.class)){
                    continue;
                }
                if (mOnExposureMethods.containsKey(clz)) {
                    OnTraceMethod onTraceMethod = mOnExposureMethods.get(clz);
                    try {
                        if (onTraceMethod.paramLength == 3) {
                            onTraceMethod.method.invoke(this, targetView, cell, type);
                            return;
                        }
                    } catch (Exception e) {
                        LogUtils.e(TAG, "Invoke onExposure method error: " + Log.getStackTraceString(e),
                                e);
                    }
                }
            }
            defaultExposureCell(targetView, cell, type);
        }
    }

    public void defaultExposureCell(@NonNull View targetView, @NonNull BaseCell cell, int type) {

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

    static class OnTraceMethod {

        int paramLength;

        Method method;

        public OnTraceMethod(int paramLength, Method method) {
            this.paramLength = paramLength;
            this.method = method;
        }
    }

    public abstract void onExposure(@NonNull Card card, int offset, int position);

    public void attachUtInfoToView(View targetView, BaseCell baseCell) {

    }

    /**
     *
     * @param rxEvent
     * @return exposure handle task by rxEvent
     */
    public RxExposureCancellable getRxExposureCancellable(TangramRxEvent rxEvent) {
        return null;
    }

    /**
     * By default,
     * @param rxEvent
     * @return
     */
    public ObservableTransformer<TangramRxEvent, TangramRxEvent> getObservableTransformer(TangramRxEvent rxEvent) {
        return null;
    }

}
