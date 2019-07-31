package com.tmall.wireless.tangram3;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.tmall.wireless.tangram3.core.protocol.ElementRenderService;
import com.tmall.wireless.tangram3.dataparser.concrete.ComponentInfo;
import com.tmall.wireless.tangram3.structure.BaseCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentRenderManager {

    private Map<String, ElementRenderService> renderServiceMap = new HashMap<>(5);

    private Map<String, ComponentInfo> componentInfoMap = new ConcurrentHashMap<>(128);

    public ElementRenderService getRenderService(String sdkName) {
        return renderServiceMap.get(sdkName);
    }

    public View createView(Context context, ViewGroup parent, ComponentInfo info) {
        if (info == null) {
            return new View(context);
        }
        return renderServiceMap.get(info.getType()).createView(context, parent, info);
    }

    public void onDownloadTemplate() {
        // we consider the count of render service in one page should not more than 4
        //FIXME so ugly!!!
        List<ComponentInfo> componentInfoList1 = null;
        List<ComponentInfo> componentInfoList2 = null;
        List<ComponentInfo> componentInfoList3 = null;
        List<ComponentInfo> componentInfoList4 = null;

        String renderService1 = null;
        String renderService2 = null;
        String renderService3 = null;
        String renderService4 = null;

        Set<String> renderSet = renderServiceMap.keySet();
        int i = 0;
        for (Iterator<String> iterator = renderSet.iterator(); iterator.hasNext(); ) {
            String renderService = iterator.next();
            if (i == 0) {
                renderService1 = renderService;
            } else if (i == 1) {
                renderService2 = renderService;
            } else if (i == 2) {
                renderService3 = renderService;
            } else if (i == 3) {
                renderService4 = renderService;
            }
            i++;
        }

        for (ComponentInfo componentInfo : componentInfoMap.values()) {
            if (!TextUtils.isEmpty(renderService1) && renderService1.equals(componentInfo.getType())) {
                if (componentInfoList1 == null) {
                    componentInfoList1 = new ArrayList<>();
                }
                componentInfoList1.add(componentInfo);
            } else if (!TextUtils.isEmpty(renderService2) && renderService2.equals(componentInfo.getType())) {
                if (componentInfoList2 == null) {
                    componentInfoList2 = new ArrayList<>();
                }
                componentInfoList2.add(componentInfo);
            } else if (!TextUtils.isEmpty(renderService3) && renderService3.equals(componentInfo.getType())) {
                if (componentInfoList3 == null) {
                    componentInfoList3 = new ArrayList<>();
                }
                componentInfoList3.add(componentInfo);
            } else if (!TextUtils.isEmpty(renderService4) && renderService4.equals(componentInfo.getType())) {
                if (componentInfoList4 == null) {
                    componentInfoList4 = new ArrayList<>();
                }
                componentInfoList4.add(componentInfo);
            } else {
                Log.e("tangram", "we consider the count of render service in one page should not more than 4!");
            }
        }

        if (!TextUtils.isEmpty(renderService1)) {
            renderServiceMap.get(renderService1).onDownloadComponentInfo(componentInfoList1);
        }

        if (!TextUtils.isEmpty(renderService2)) {
            renderServiceMap.get(renderService2).onDownloadComponentInfo(componentInfoList2);
        }

        if (!TextUtils.isEmpty(renderService3)) {
            renderServiceMap.get(renderService3).onDownloadComponentInfo(componentInfoList3);
        }

        if (!TextUtils.isEmpty(renderService4)) {
            renderServiceMap.get(renderService4).onDownloadComponentInfo(componentInfoList4);
        }
    }

    public boolean mountView(BaseCell cell, View view) {
        boolean ret = false;
        if (cell.componentInfo != null) {
            ret = renderServiceMap.get(cell.componentInfo.getType()).mountView(cell.extras, view);
        }
        return ret;
    }

    public void unmountView(BaseCell cell, View view) {
        if (cell.componentInfo != null) {
            renderServiceMap.get(cell.componentInfo.getType()).unmountView(cell.extras, view);
        }
    }

    public void addRenderService(ElementRenderService renderService) {
        this.renderServiceMap.put(renderService.getSDKBizName(), renderService);
    }

    public void destroyRenderService() {
        for (ElementRenderService renderService : renderServiceMap.values()) {
            renderService.destroy();
        }
    }

    public void putComponentInfo(ComponentInfo info) {
        renderServiceMap.get(info.getType()).onParseComponentInfo(info);
        componentInfoMap.put(info.getId(), info);
    }

    public ComponentInfo getComponentInfo(String id) {
        return componentInfoMap.get(id);
    }

    public Map<String, ComponentInfo> getComponentInfoMap() {
        return componentInfoMap;
    }

    public ComponentInfo supplementComponentInfo(String cellType) {
        ComponentInfo componentInfo = null;
        if (componentInfoMap.containsKey(cellType)) {
            componentInfo = componentInfoMap.get(cellType);
        } else {
            for (ElementRenderService renderService : renderServiceMap.values()) {
                componentInfo = renderService.supplementComponentInfo(cellType);
                if (componentInfo != null) {
                    break;
                }
            }
        }
        return componentInfo;
    }
}
