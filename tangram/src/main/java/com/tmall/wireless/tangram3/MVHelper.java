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

package com.tmall.wireless.tangram3;

import android.os.Build.VERSION;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.wireless.tangram.core.R;
import com.tmall.wireless.tangram3.core.service.ServiceManager;
import com.tmall.wireless.tangram3.dataparser.concrete.BaseCellBinderResolver;
import com.tmall.wireless.tangram3.structure.BaseCell;
import com.tmall.wireless.tangram3.structure.view.ITangramViewLifeCycle;
import com.tmall.wireless.tangram3.support.CellSupport;
import com.tmall.wireless.tangram3.support.ExposureSupport;
import com.tmall.wireless.tangram3.util.BDE;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.tmall.wireless.tangram.dataparser.concrete.Style.MARGIN_BOTTOM_INDEX;
import static com.tmall.wireless.tangram.dataparser.concrete.Style.MARGIN_LEFT_INDEX;
import static com.tmall.wireless.tangram.dataparser.concrete.Style.MARGIN_RIGHT_INDEX;
import static com.tmall.wireless.tangram.dataparser.concrete.Style.MARGIN_TOP_INDEX;

/**
 * Created by mikeafc on 16/4/25.
 */
public class MVHelper {
    private static final String TAG = "Tangram-MVHelper";

    public static final String DEFAULT_ENGINE_TAG = "default_tag";

    private String engineTag = DEFAULT_ENGINE_TAG;

    private MVResolver mvResolver;

    private ComponentRenderManager renderManager;

    private Map<BaseCell, Method> postBindMap = new HashMap<>(128);
    private Map<BaseCell, Method> postUnBindMap = new HashMap<>(128);
    private Map<BaseCell, Method> cellInitedMap = new HashMap<>(128);

    public MVHelper(MVResolver mvResolver) {
        this.mvResolver = mvResolver;
        renderManager = new ComponentRenderManager();
    }

    public MVResolver resolver() {
        return mvResolver;
    }

    public ComponentRenderManager renderManager() {
        return renderManager;
    }

    /**
     * FIXME sholud be called after original component's postUnBind method excuted
     */
    public void reset() {
        postBindMap.clear();
        postUnBindMap.clear();
        cellInitedMap.clear();
        mvResolver.reset();
    }

    public boolean isValid(BaseCell cell, ServiceManager serviceManager) {
        boolean ret = cell.isValid();
        if (serviceManager != null) {
            BaseCellBinderResolver componentBinderResolver = serviceManager.getService(BaseCellBinderResolver.class);
            ret = ret && (componentBinderResolver.has(cell.stringType) || cell.componentInfo != null && renderManager.getRenderService(cell.componentInfo.getType()) != null);
            CellSupport cellSupport = serviceManager.getService(CellSupport.class);
            if (cellSupport != null) {
                ret = cellSupport.isValid(cell) && ret;
            }
        }
        return ret;
    }

    public void mountView(BaseCell cell, View view) {
        try {
            mvResolver.register(cell, view);
            if (view.getTag(R.id.TANGRAM_ENGINE_TAG) == null) {
                view.setTag(R.id.TANGRAM_ENGINE_TAG, engineTag);
            }
            if (cell.serviceManager != null) {
                if (cell.serviceManager.supportRx()) {
                    cell.emitNext(BDE.BIND);
                }
                CellSupport cellSupport = cell.serviceManager.getService(CellSupport.class);
                if (cellSupport != null) {
                    cellSupport.bindView(cell, view);
                }
            }
            boolean renderServiceSuccess = renderManager.mountView(cell, view);
            if (!renderServiceSuccess) {
                initView(cell, view);
            }
            renderStyle(cell, view);
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

    public void unMountView(BaseCell cell, View view) {
        renderManager.unmountView(cell, view);
        if (cell.serviceManager != null) {
            if (cell.serviceManager.supportRx()) {
                cell.emitNext(BDE.UNBIND);
            }
        }
        postUnMountView(cell, view);
        if (cell.serviceManager != null) {
            CellSupport cellSupport = cell.serviceManager.getService(CellSupport.class);
            if (cellSupport != null) {
                cellSupport.unBindView(cell, view);
            }
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
                layoutParams.topMargin = cell.style.margin[MARGIN_TOP_INDEX];
                layoutParams.leftMargin = cell.style.margin[MARGIN_LEFT_INDEX];
                layoutParams.bottomMargin = cell.style.margin[MARGIN_BOTTOM_INDEX];
                layoutParams.rightMargin = cell.style.margin[MARGIN_RIGHT_INDEX];
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

    public String getEngineTag() {
        return engineTag;
    }

    public void setEngineTag(String engineTag) {
        this.engineTag = engineTag;
    }
}
