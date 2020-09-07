package com.tmall.wireless.tangram3.core.protocol;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.tmall.wireless.tangram3.TangramEngine;
import com.tmall.wireless.tangram3.dataparser.concrete.ComponentInfo;

import java.util.List;

public abstract class ElementRenderService {

    abstract public void init(TangramEngine tangramEngine);

    abstract public View createView(Context context, ViewGroup parent, ComponentInfo info);

    abstract public boolean mountView(JSONObject json, View view);

    abstract public void unmountView(JSONObject json, View view);

    /**
     * Tangram would call this method to ask renderService supple component info
     * when the data json does not have a component info.
     *
     * @param cellType
     * @return
     */
    public ComponentInfo supplementComponentInfo(String cellType) {
        return null;
    }

    abstract public void destroy();

    abstract public String getSDKBizName();

    abstract public ComponentInfo onParseComponentInfo(ComponentInfo info);

    abstract public void onDownloadComponentInfo(List<ComponentInfo> componentInfoList);

    public String getItemViewType(String type, ComponentInfo componentInfo) {
        return null;
    }
}