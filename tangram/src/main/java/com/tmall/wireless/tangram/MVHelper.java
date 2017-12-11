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

package com.tmall.wireless.tangram;

import com.alibaba.android.vlayout.VirtualLayoutManager;

import android.os.Build.VERSION;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.CellRender;
import com.tmall.wireless.tangram.structure.view.ITangramViewLifeCycle;
import com.tmall.wireless.tangram.support.CellSupport;
import com.tmall.wireless.tangram.support.ExposureSupport;

import com.tmall.wireless.vaf.framework.VafContext;
import com.tmall.wireless.vaf.virtualview.core.IContainer;
import com.tmall.wireless.vaf.virtualview.core.ViewBase;
import com.tmall.wireless.vaf.virtualview.event.EventData;
import com.tmall.wireless.vaf.virtualview.event.EventManager;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Method;

import static com.tmall.wireless.tangram.dataparser.concrete.Style.MARGIN_BOTTOM_INDEX;
import static com.tmall.wireless.tangram.dataparser.concrete.Style.MARGIN_LEFT_INDEX;
import static com.tmall.wireless.tangram.dataparser.concrete.Style.MARGIN_RIGHT_INDEX;
import static com.tmall.wireless.tangram.dataparser.concrete.Style.MARGIN_TOP_INDEX;

/**
 * Created by mikeafc on 16/4/25.
 */
public class MVHelper {
    private static final String TAG = "Tangram-MVHelper";

    private MVResolver mvResolver;

    private VafContext mVafContext;

    private ArrayMap<BaseCell, ArrayMap<Method, Object>> methodMap = new ArrayMap<>(128);
    private ArrayMap<Class, Method[]> methodCacheMap = new ArrayMap<>(128);
    private ArrayMap<BaseCell, Method> postBindMap = new ArrayMap<>(128);
    private ArrayMap<BaseCell, Method> postUnBindMap = new ArrayMap<>(128);
    private ArrayMap<BaseCell, Method> cellInitedMap = new ArrayMap<>(128);
    private ArrayMap<BaseCell, String> cellFlareIdMap = new ArrayMap<>(128);

    public MVHelper(MVResolver mvResolver) {
        this.mvResolver = mvResolver;
    }

    public MVResolver resolver() {
        return mvResolver;
    }

    public VafContext getVafContext() {
        return mVafContext;
    }

    public void setVafContext(VafContext vafContext) {
        mVafContext = vafContext;
    }

    public void parseCell(MVHelper resolver, BaseCell cell, JSONObject json) {
        mvResolver.parseCell(resolver, cell, json);
    }

    /**
     * FIXME sholud be called after original component's postUnBind method excuted
     */
    public void reset() {
        methodMap.clear();
        postBindMap.clear();
        postUnBindMap.clear();
        cellInitedMap.clear();
        cellFlareIdMap.clear();
        mvResolver.reset();
    }

    public boolean isValid(BaseCell cell, ServiceManager serviceManager) {
        if (serviceManager != null) {
            CellSupport cellSupport = serviceManager.getService(CellSupport.class);
            if (cellSupport != null) {
                return cellSupport.isValid(cell) && cell.isValid();
            }
        }
        return cell.isValid();
    }

    public void mountView(BaseCell cell, View view) {
        try {
            mvResolver.register(getCellUniqueId(cell), cell, view);
            if (cell.serviceManager != null) {
                CellSupport cellSupport = cell.serviceManager.getService(CellSupport.class);
                if (cellSupport != null) {
                    cellSupport.bindView(cell, view);
                }
            }
            if (view instanceof IContainer) {
                ViewBase vb = ((IContainer)view).getVirtualView();
                vb.setVData(cell.extras);
                if (vb.supportExposure()) {
                    VafContext context = cell.serviceManager.getService(VafContext.class);
                    context.getEventManager().emitEvent(
                        EventManager.TYPE_Exposure, EventData.obtainData(context, vb));
                }
                renderStyle(cell, view);
            } else {
                loadMethod(cell, view);
                initView(cell, view);
                renderView(cell, view);
                renderStyle(cell, view);
            }
            if (resolver().isCompatibleType(cell.stringType)) {
                resolver().getCellClass(cell.stringType).cast(cell).bindView(view);
            }
            postMountView(cell, view);
            if (cell.serviceManager != null) {
                CellSupport cellSupport = cell.serviceManager.getService(CellSupport.class);
                if (cellSupport != null) {
                    cellSupport.postBindView(cell, view);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cell.serviceManager != null) {
                CellSupport cellSupport = cell.serviceManager.getService(CellSupport.class);
                if (cellSupport != null) {
                    cellSupport.onBindViewException(cell, view, e);
                }
            }
        }
    }

    public String getCellUniqueId(BaseCell cell) {
        String flareId = cellFlareIdMap.get(cell);
        if (flareId == null) {
            String parentId = "";
            if (cell.parent instanceof Card) {
                parentId = ((Card) cell.parent).id;
            } else if (cell.nestedParent instanceof BaseCell) {
                parentId = ((BaseCell) cell.nestedParent).id;
            }
            flareId = String.format("%s_%s", cell.parent == null ? "null" : parentId, cell.pos);
            cellFlareIdMap.put(cell, flareId);
        }
        return flareId;
    }

    public void unMountView(BaseCell cell, View view) {
        if (view instanceof IContainer) {
            ViewBase vb = ((IContainer)view).getVirtualView();
            vb.reset();
        }
        postUnMountView(cell, view);
        if (cell.serviceManager != null) {
            CellSupport cellSupport = cell.serviceManager.getService(CellSupport.class);
            if (cellSupport != null) {
                cellSupport.unBindView(cell, view);
            }
        }
        if (resolver().isCompatibleType(cell.stringType)) {
            resolver().getCellClass(cell.stringType).cast(cell).unbindView(view);
        }
    }

    private void loadMethod(BaseCell cell, View view) {
        if (view instanceof ITangramViewLifeCycle) {
            return;
        }
        if (methodMap.get(cell) != null) {
            return;
        }

        ArrayMap<Method, Object> paramMap = new ArrayMap<>();

        Method[] methods;
        if (methodCacheMap.get(view.getClass()) == null) {
            methods = view.getClass().getDeclaredMethods();
            methodCacheMap.put(view.getClass(), methods);
        } else {
            methods = methodCacheMap.get(view.getClass());
        }
        CellRender cellRender;
        Class[] paramClazz;
        for (Method method : methods) {
            cellRender = method.getAnnotation(CellRender.class);
            paramClazz = method.getParameterTypes();

            if (!method.isAnnotationPresent(CellRender.class) ||
                paramClazz == null || paramClazz.length != 1) {
                continue;
            }

            if (method.getName().equals("postBindView")) {
                postBindMap.put(cell, method);
                continue;
            }

            if (method.getName().equals("postUnBindView")) {
                postUnBindMap.put(cell, method);
                continue;
            }

            if (method.getName().equals("cellInited")) {
                cellInitedMap.put(cell, method);
                continue;
            }

            if (!TextUtils.isEmpty(cellRender.key()) && cell.hasParam(cellRender.key())) {
                if ("null".equals(cell.optParam(cellRender.key()))) {
                    paramMap.put(method, null);
                } else if (paramClazz[0].equals(Integer.class) || paramClazz[0].equals(int.class)) {
                    paramMap.put(method, cell.optIntParam(cellRender.key()));
                } else if (paramClazz[0].equals(String.class)) {
                    paramMap.put(method, cell.optStringParam(cellRender.key()));
                } else if (paramClazz[0].equals(Boolean.class) || paramClazz[0].equals(boolean.class)) {
                    paramMap.put(method, cell.optBoolParam(cellRender.key()));
                } else if (paramClazz[0].equals(Double.class) || paramClazz[0].equals(double.class)) {
                    paramMap.put(method, cell.optDoubleParam(cellRender.key()));
                } else if (paramClazz[0].equals(JSONArray.class)) {
                    paramMap.put(method, cell.optJsonArrayParam(cellRender.key()));
                } else if (paramClazz[0].equals(Long.class) || paramClazz[0].equals(long.class)) {
                    paramMap.put(method, cell.optLongParam(cellRender.key()));
                } else if (paramClazz[0].equals(JSONObject.class)) {
                    paramMap.put(method, cell.optJsonObjectParam(cellRender.key()));
                } else {
                    paramMap.put(method, cell.optParam(cellRender.key()));
                }
            } else if (cell.hasParam(method.getName())) {
                if ("null".equals(cell.optParam(method.getName()))) {
                    paramMap.put(method, null);
                } else if (paramClazz[0].equals(Integer.class) || paramClazz[0].equals(int.class)) {
                    paramMap.put(method, cell.optIntParam(method.getName()));
                } else if (paramClazz[0].equals(String.class)) {
                    paramMap.put(method, cell.optStringParam(method.getName()));
                } else if (paramClazz[0].equals(Boolean.class) || paramClazz[0].equals(boolean.class)) {
                    paramMap.put(method, cell.optBoolParam(method.getName()));
                } else if (paramClazz[0].equals(Double.class) || paramClazz[0].equals(double.class)) {
                    paramMap.put(method, cell.optDoubleParam(method.getName()));
                } else if (paramClazz[0].equals(JSONArray.class)) {
                    paramMap.put(method, cell.optJsonArrayParam(method.getName()));
                } else if (paramClazz[0].equals(Long.class) || paramClazz[0].equals(long.class)) {
                    paramMap.put(method, cell.optLongParam(method.getName()));
                } else if (paramClazz[0].equals(JSONObject.class)) {
                    paramMap.put(method, cell.optJsonObjectParam(method.getName()));
                } else {
                    paramMap.put(method, cell.optParam(method.getName()));
                }
            } else {
                if (paramClazz[0].equals(Integer.class) || paramClazz[0].equals(int.class)) {
                    paramMap.put(method, 0);
                } else if (paramClazz[0].equals(String.class)) {
                    paramMap.put(method, "");
                } else if (paramClazz[0].equals(Boolean.class) || paramClazz[0].equals(boolean.class)) {
                    paramMap.put(method, false);
                } else if (paramClazz[0].equals(Double.class) || paramClazz[0].equals(double.class)) {
                    paramMap.put(method, 0);
                } else if (paramClazz[0].equals(JSONArray.class)) {
                    paramMap.put(method, null);
                } else if (paramClazz[0].equals(Long.class) || paramClazz[0].equals(long.class)) {
                    paramMap.put(method, 0);
                } else if (paramClazz[0].equals(JSONObject.class)) {
                    paramMap.put(method, null);
                } else {
                    paramMap.put(method, "");
                }
            }
        }

        if (!paramMap.isEmpty()) {
            methodMap.put(cell, paramMap);
        }
    }

    private void initView(BaseCell cell, View view) {
        if (view instanceof ITangramViewLifeCycle) {
            ((ITangramViewLifeCycle) view).cellInited(cell);
        } else {
            if (cellInitedMap.get(cell) != null) {
                try {
                    cellInitedMap.get(cell).invoke(view, cell);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void renderView(BaseCell cell, View view) {
        if (view instanceof ITangramViewLifeCycle) {
            return;
        }
        if (methodMap.get(cell) == null) {
            return;
        }
        for (Method method : methodMap.get(cell).keySet()) {
            try {
                method.invoke(view, methodMap.get(cell).get(method));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void renderStyle(BaseCell cell, View view) {
        renderLayout(cell, view);
        renderBackground(cell, view);
    }

    protected void renderLayout(BaseCell cell, View view) {
        if (cell.style != null) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();

            if (lp == null || !(lp instanceof VirtualLayoutManager.LayoutParams)) {
                if (lp == null) {
                    lp = new VirtualLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                } else {
                    lp = new VirtualLayoutManager.LayoutParams(lp.width, lp.height);
                }
                view.setLayoutParams(lp);
            }
            if (lp instanceof VirtualLayoutManager.LayoutParams) {
                VirtualLayoutManager.LayoutParams params = (VirtualLayoutManager.LayoutParams) lp;

                if (cell.style.height >= 0) {
                    params.storeOriginHeight();
                    params.height = cell.style.height;
                } else {
                    params.restoreOriginHeight();
                }

                if (cell.style.width >= 0) {
                    params.storeOriginWidth();
                    params.width = cell.style.width;
                } else {
                    params.restoreOriginWidth();
                }

                params.mAspectRatio = cell.style.aspectRatio;

                params.zIndex = cell.style.zIndex;
                if (params.zIndex == 0) {
                    if (cell.parent != null && cell.parent.style != null) {
                        params.zIndex = cell.parent.style.zIndex;
                    }
                }
                if (VERSION.SDK_INT >= 21) {
                    view.setZ(params.zIndex);
                }
            } else {
                if (cell.style.height >= 0) {
                    lp.height = cell.style.height;
                }

                if (cell.style.width >= 0) {
                    lp.width = cell.style.width;
                }
            }


            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) lp;
                layoutParams.topMargin = cell.style.margin[MARGIN_TOP_INDEX] >= 0 ? cell.style.margin[MARGIN_TOP_INDEX] : 0;
                layoutParams.leftMargin = cell.style.margin[MARGIN_LEFT_INDEX] >= 0 ? cell.style.margin[MARGIN_LEFT_INDEX] : 0;
                layoutParams.bottomMargin = cell.style.margin[MARGIN_BOTTOM_INDEX] >= 0 ? cell.style.margin[MARGIN_BOTTOM_INDEX] : 0;
                layoutParams.rightMargin = cell.style.margin[MARGIN_RIGHT_INDEX] >= 0 ? cell.style.margin[MARGIN_RIGHT_INDEX] : 0;
            }

            // reset translation animation before reused
            view.setTranslationX(0);
            view.setTranslationY(0);
        }
    }

    protected void renderBackground(BaseCell cell, View view) {
        if (cell.style != null) {
            if (cell.style.bgColor != 0) {
                view.setBackgroundColor(cell.style.bgColor);
            }
        }
    }

    private void postMountView(BaseCell cell, View view) {
        if (!cell.mIsExposed && cell.serviceManager != null) {
            ExposureSupport exposureSupport = cell.serviceManager.getService(ExposureSupport.class);
            if (exposureSupport != null) {
                cell.mIsExposed = true;
                exposureSupport.onExposure(view, cell, cell.pos);
            }
        }
        if (view instanceof ITangramViewLifeCycle) {
            ((ITangramViewLifeCycle) view).postBindView(cell);
        } else {
            if (postBindMap.get(cell) != null) {
                try {
                    postBindMap.get(cell).invoke(view, cell);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (resolver().isCompatibleType(cell.stringType)) {
            resolver().getCellClass(cell.stringType).cast(cell).postBindView(view);
        }
    }

    private void postUnMountView(BaseCell cell, View view) {
        if (view instanceof ITangramViewLifeCycle) {
            ((ITangramViewLifeCycle) view).postUnBindView(cell);
        } else {
            if (postUnBindMap.get(cell) != null) {
                try {
                    postUnBindMap.get(cell).invoke(view, cell);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
