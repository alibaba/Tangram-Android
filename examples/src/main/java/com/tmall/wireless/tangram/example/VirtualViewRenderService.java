package com.tmall.wireless.tangram.example;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.tmall.wireless.tangram.TangramEngine;
import com.tmall.wireless.tangram.core.protocol.ElementRenderService;
import com.tmall.wireless.tangram.dataparser.concrete.ComponentInfo;
import com.tmall.wireless.tangram.example.data.DEBUG;
import com.tmall.wireless.tangram.example.data.VVTEST;
import com.tmall.wireless.vaf.framework.VafContext;
import com.tmall.wireless.vaf.virtualview.Helper.ImageLoader;
import com.tmall.wireless.vaf.virtualview.core.IContainer;
import com.tmall.wireless.vaf.virtualview.core.ViewBase;
import com.tmall.wireless.vaf.virtualview.event.EventData;
import com.tmall.wireless.vaf.virtualview.event.EventManager;
import com.tmall.wireless.vaf.virtualview.view.image.ImageBase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VirtualViewRenderService extends ElementRenderService {
    private VafContext vafContext;

    private TangramEngine tangramEngine;

    private static class ImageTarget implements Target {

        ImageBase mImageBase;

        ImageLoader.Listener mListener;

        public ImageTarget(ImageBase imageBase) {
            mImageBase = imageBase;
        }

        public ImageTarget(ImageLoader.Listener listener) {
            mListener = listener;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mImageBase.setBitmap(bitmap, true);
            if (mListener != null) {
                mListener.onImageLoadSuccess(bitmap);
            }
            Log.d("TangramActivity", "onBitmapLoaded " + from);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if (mListener != null) {
                mListener.onImageLoadFailed();
            }
            Log.d("TangramActivity", "onBitmapFailed ");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.d("TangramActivity", "onPrepareLoad ");
        }
    }

    private ImageLoader.IImageLoaderAdapter imageLoaderAdapter = new ImageLoader.IImageLoaderAdapter() {

        private List<ImageTarget> cache = new ArrayList<ImageTarget>();

        @Override
        public void bindImage(String uri, final ImageBase imageBase, int reqWidth, int reqHeight) {
            RequestCreator requestCreator = Picasso.with(tangramEngine.getContext()).load(uri);
            Log.d("TangramActivity", "bindImage request width height " + reqHeight + " " + reqWidth);
            if (reqHeight > 0 || reqWidth > 0) {
                requestCreator.resize(reqWidth, reqHeight);
            }
            ImageTarget imageTarget = new ImageTarget(imageBase);
            cache.add(imageTarget);
            requestCreator.into(imageTarget);
        }

        @Override
        public void getBitmap(String uri, int reqWidth, int reqHeight, final ImageLoader.Listener lis) {
            RequestCreator requestCreator = Picasso.with(tangramEngine.getContext()).load(uri);
            Log.d("TangramActivity", "getBitmap request width height " + reqHeight + " " + reqWidth);
            if (reqHeight > 0 || reqWidth > 0) {
                requestCreator.resize(reqWidth, reqHeight);
            }
            ImageTarget imageTarget = new ImageTarget(lis);
            cache.add(imageTarget);
            requestCreator.into(imageTarget);
        }
    };

    @Override
    public void init(TangramEngine tangramEngine) {
        this.tangramEngine = tangramEngine;
        vafContext = new VafContext(tangramEngine.getContext());
        tangramEngine.register(VafContext.class, vafContext);
        vafContext.getViewManager().init(tangramEngine.getContext());
        vafContext.setImageLoaderAdapter(imageLoaderAdapter);
        vafContext.getViewManager().loadBinBufferSync(VVTEST.BIN);
        vafContext.getViewManager().loadBinBufferSync(DEBUG.BIN);
    }

    @Override
    public View createView(Context context, ViewGroup parent, ComponentInfo info) {
        return vafContext.getContainerService().getContainer(info.getName(), true);
    }

    @Override
    public boolean mountView(JSONObject json, View view) {
        ViewBase vb = ((IContainer) view).getVirtualView();
        vb.setVData(json);
        if (vb.supportExposure()) {
            vafContext.getEventManager().emitEvent(
                    EventManager.TYPE_Exposure, EventData.obtainData(vafContext, vb));
        }

        return true;
    }

    @Override
    public void unmountView(JSONObject json, View view) {
        if (view instanceof IContainer) {
            ViewBase vb = ((IContainer) view).getVirtualView();
            vb.reset();
        }
    }

    @Override
    public void destroy() {
        if (vafContext != null) {
            vafContext.onDestroy();
        }
    }

    @Override
    public ComponentInfo onParseComponentInfo(ComponentInfo info) {
        return info;
    }

    @Override
    public String getSDKBizName() {
        return "VirtualView";
    }

    @Override
    public void onDownloadComponentInfo(List<ComponentInfo> componentInfoList) {

    }
}
