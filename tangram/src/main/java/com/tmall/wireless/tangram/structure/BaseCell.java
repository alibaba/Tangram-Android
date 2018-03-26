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

package com.tmall.wireless.tangram.structure;

import android.util.SparseArray;
import com.tmall.wireless.tangram.Engine;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.ComponentLifecycle;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.op.UpdateCellOp;
import com.tmall.wireless.tangram.support.ExposureSupport;
import com.tmall.wireless.tangram.support.RxExposureCancellable;
import com.tmall.wireless.tangram.support.RxClickExposureEvent;
import com.tmall.wireless.tangram.support.RxTangramSupport;
import com.tmall.wireless.tangram.support.SimpleClickSupport;
import com.tmall.wireless.tangram.support.ViewClickObservable;
import com.tmall.wireless.tangram.support.ViewExposureObservable;
import com.tmall.wireless.tangram.util.IInnerImageSetter;
import com.tmall.wireless.tangram.util.ImageUtils;

import com.tmall.wireless.vaf.virtualview.core.ViewCache;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mikeafc on 16/4/25.
 */
public class BaseCell<V extends View> extends ComponentLifecycle implements View.OnClickListener {
    private static AtomicLong sIdGen = new AtomicLong();

    public static boolean sIsGenIds = false;

    /**
     * cell's type, use {@link #stringType} instead
     */
    @Deprecated
    public int type;

    /**
     * cell's type
     */
    public String stringType;

    /**
     * parent's id
     */
    @Nullable
    public String parentId;

    /**
     * parent
     */
    public Card parent;

    /**
     * id of a cell
     */
    @Nullable
    public String id;

    /**
     * the natural position this cell in its parent
     */
    public int pos;

    /**
     * position that assigned from server side
     */
    public int position = -1;

    /**
     * cell's style
     */
    @Nullable
    public Style style;

    /**
     * item type for adapter.<br />
     * by default, the item type is calculated by {@link #type}, which means cell with same type share a recycler pool.
     * if you set a unique typeKey to cell, the item type is calculated by {@link #typeKey}, which measn cells with same typeKey share a recycler pool. This may be dangerous you must ensure the same typeKey must be assigned to the same type of cell.<br />
     * best practice is that if you have 10 cells with same type and need a certain one to a independent recycler pool.
     */
    public String typeKey;

    /**
     * inner use, item id for adapter.
     */
    public final long objectId;

    /**
     * the original json data
     */
    public JSONObject extras = new JSONObject();

    private ArrayMap<String, Object> bizParaMap = new ArrayMap<>(32);

    private ArrayMap<Integer, Integer> innerClickMap = new ArrayMap<>();

    @Nullable
    public ServiceManager serviceManager;

    public boolean mIsExposed = false;

    private SparseArray<Object> mTag;

    private PublishSubject<UpdateCellOp> mUpdateCellOpObservable = PublishSubject.create();

    public BaseCell() {
        objectId = sIsGenIds ? sIdGen.getAndIncrement() : 0;
    }

    /**
     * Use {@link #BaseCell(String)} instead
     * @param type
     */
    @Deprecated
    public BaseCell(int type) {
        this.type = type;
        this.stringType = String.valueOf(type);
        objectId = sIsGenIds ? sIdGen.getAndIncrement() : 0;
    }

    public BaseCell(String stringType) {
        setStringType(stringType);
        objectId = sIsGenIds ? sIdGen.getAndIncrement() : 0;
    }

    public void setStringType(String type) {
        stringType = type;
        try {
            this.type = Integer.parseInt(type);
        } catch (NumberFormatException e) {
        }
    }

    public void addBizParam(String key, Object value) {
        bizParaMap.put(key, value);
    }

    public Map<String, Object> getAllBizParams() {
        return bizParaMap;
    }

    @Override
    public void onClick(View v) {
        if (serviceManager != null) {
            SimpleClickSupport service = serviceManager.getService(SimpleClickSupport.class);
            if (service != null) {
                int eventType = this.pos;
                if (innerClickMap.containsKey(v.hashCode())) {
                    eventType = innerClickMap.get(v.hashCode()).intValue();
                }
                service.onClick(v, this, eventType);
            }
        }
    }

    public void setOnClickListener(View view, int eventType) {
        view.setOnClickListener(this);
        innerClickMap.put(view.hashCode(), Integer.valueOf(eventType));
    }

    public void clearClickListener(View view, int eventType) {
        view.setOnClickListener(null);
        innerClickMap.remove(view.hashCode());
    }

    /**
     * Do not call this method as its poor performance.
     */
    @Deprecated
    public final void notifyDataChange() {
        if (serviceManager instanceof Engine) {
            ((Engine) serviceManager).refresh(false);
        }
    }


    public final void doLoadImageUrl(ImageView view, String imgUrl) {
        if (serviceManager != null && serviceManager.getService(IInnerImageSetter.class) != null) {
            serviceManager.getService(IInnerImageSetter.class).doLoadImageUrl(view, imgUrl);
        } else {
            ImageUtils.doLoadImageUrl(view, imgUrl);
        }
    }

    public boolean hasParam(String key) {
        return extras.has(key) ||
            style != null && style.extras != null && style.extras.has(key);
    }

    public Object optParam(String key) {
        if (extras.has(key)) {
            return extras.opt(key);
        }
        if (style != null && style.extras != null) {
            return style.extras.opt(key);
        }
        return null;
    }

    public long optLongParam(String key) {
        if (extras.has(key)) {
            return extras.optLong(key);
        }
        if (style != null && style.extras != null) {
            return style.extras.optLong(key);
        }
        return 0;
    }

    public int optIntParam(String key) {
        if (extras.has(key)) {
            return extras.optInt(key);
        }
        if (style != null && style.extras != null) {
            return style.extras.optInt(key);
        }
        return 0;
    }

    public String optStringParam(String key) {
        if (extras.has(key)) {
            return extras.optString(key);
        }
        if (style != null && style.extras != null) {
            return style.extras.optString(key);
        }
        return "";
    }

    public double optDoubleParam(String key) {
        if (extras.has(key)) {
            return extras.optDouble(key);
        }
        if (style != null && style.extras != null) {
            return style.extras.optDouble(key);
        }
        return Double.NaN;
    }

    public boolean optBoolParam(String key) {
        if (extras.has(key)) {
            return extras.optBoolean(key);
        }
        return style != null && style.extras != null && style.extras.optBoolean(key);
    }

    public JSONObject optJsonObjectParam(String key) {
        if (extras.has(key)) {
            return extras.optJSONObject(key);
        }
        if (style != null && style.extras != null) {
            return style.extras.optJSONObject(key);
        }
        return null;
    }

    public JSONArray optJsonArrayParam(String key) {
        if (extras.has(key)) {
            return extras.optJSONArray(key);
        }
        if (style != null && style.extras != null) {
            return style.extras.optJSONArray(key);
        }
        return null;
    }

    /***
     * for compatible
     */
    @Deprecated
    public void parseWith(JSONObject data) {

    }

    /***
     * for compatible
     */
    public void parseStyle(@Nullable JSONObject data) {

    }

    public void parseWith(@NonNull JSONObject data, @NonNull final MVHelper resolver) {

    }

    /***
     * for compatible
     */
    public void bindView(@NonNull V view) {

    }

    /***
     * for compatible
     */
    public void postBindView(@NonNull V view) {

    }

    /***
     * for compatible
     */
    public void unbindView(@NonNull V view) {
        clearClickListener(view, 0);
    }

    /***
     * for compatible
     */
    public boolean isValid() {
        return true;
    }

    /**
     * bind a tag to baseCell
     * @param key
     * @param value
     */
    public void setTag(int key, Object value) {
        if (mTag == null) {
            mTag = new SparseArray<>();
        }
        mTag.put(key, value);
    }

    /**
     * get a tag from baseCell
     * @param key
     * @return
     */
    public Object getTag(int key) {
        if (mTag != null) {
            return mTag.get(key);
        }
        return null;
    }

    @Override
    public void onAdded() {
        super.onAdded();
        if (serviceManager != null) {
            RxTangramSupport rxTangramSupport = serviceManager.getService(RxTangramSupport.class);
            rxTangramSupport.observeCell(mUpdateCellOpObservable);
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
    }

    private ArrayMap<View, RxClickExposureEvent> mRxExposureEvents = new ArrayMap<>();

    private ArrayMap<View, Disposable> mExposureDisposables = new ArrayMap<>();

    private ArrayMap<View, ViewExposureObservable> mViewExposureObservables = new ArrayMap<>();

    /**
     * @param targetView
     * @param rxClickExposureEvent
     * @since 3.0.0
     */
    public void exposure(View targetView, RxClickExposureEvent rxClickExposureEvent) {
        final ExposureSupport exposureSupport = serviceManager.getService(ExposureSupport.class);
        RxExposureCancellable exposureCancellable = null;
        if (exposureSupport != null) {
            exposureCancellable = exposureSupport.getRxExposureCancellable(rxClickExposureEvent);
        }
        ViewExposureObservable viewExposureObservable = mViewExposureObservables.get(targetView);
        if (viewExposureObservable == null) {
            viewExposureObservable = new ViewExposureObservable(rxClickExposureEvent, exposureCancellable);
            mViewExposureObservables.put(targetView, viewExposureObservable);
        } else {
            viewExposureObservable.setRxClickExposureEvent(rxClickExposureEvent);
            viewExposureObservable.setRxExposureCancellable(exposureCancellable);
        }
        if (exposureSupport != null && exposureCancellable != null) {
            ObservableTransformer<RxClickExposureEvent, RxClickExposureEvent> transformer = exposureSupport.getObservableTransformer(

                rxClickExposureEvent);
            Disposable exposureDisposable;
            if (transformer != null) {
                exposureDisposable = viewExposureObservable.compose(transformer).subscribe(exposureCancellable);
            } else {
                exposureDisposable = viewExposureObservable.subscribe(exposureCancellable);
            }
            mExposureDisposables.put(targetView, exposureDisposable);
        }
    }

    /**
     * @param targetView
     * @since 3.0.0
     */
    public void exposure(View targetView) {
        RxClickExposureEvent rxExposureEvent = mRxExposureEvents.get(targetView);
        if (rxExposureEvent == null) {
            rxExposureEvent = new RxClickExposureEvent(targetView, this, this.pos);
            mRxExposureEvents.put(targetView, rxExposureEvent);
        } else {
            rxExposureEvent.update(targetView, this, this.pos);
        }
        exposure(targetView, rxExposureEvent);
    }

    /**
     * @param view
     * @since 3.0.0
     */
    public void unexposure(View view) {
        Disposable exposureDisposable = mExposureDisposables.get(view);
        if (exposureDisposable != null) {
            exposureDisposable.dispose();
        }
    }

    private ArrayMap<View, RxClickExposureEvent> mRxClickEvents = new ArrayMap<>();

    private ArrayMap<View, Disposable> mClickDisposables = new ArrayMap<>();

    private ArrayMap<View, ViewClickObservable> mViewClickObservables = new ArrayMap<>();

    /**
     * @param view
     * @param rxClickExposureEvent
     * @since 3.0.0
     */
    public void click(View view, RxClickExposureEvent rxClickExposureEvent) {
        ViewClickObservable viewClickObservable = mViewClickObservables.get(view);
        if (viewClickObservable == null) {
            viewClickObservable = new ViewClickObservable(rxClickExposureEvent);
            mViewClickObservables.put(view, viewClickObservable);
        } else {
            viewClickObservable.setRxClickExposureEvent(rxClickExposureEvent);
        }
        if (serviceManager != null) {
            final SimpleClickSupport service = serviceManager.getService(SimpleClickSupport.class);
            if (service != null) {
                Disposable clickDisposable = service.onRxClick(viewClickObservable, rxClickExposureEvent);
                mClickDisposables.put(view, clickDisposable);
            }
        }
    }

    /**
     * @param view
     * @since 3.0.0
     */
    public void click(View view) {
        RxClickExposureEvent rxClickEvent = mRxClickEvents.get(view);
        if (rxClickEvent == null) {
            rxClickEvent = new RxClickExposureEvent(view, this, this.pos);
            mRxClickEvents.put(view, rxClickEvent);
        } else {
            rxClickEvent.update(view, this, this.pos);
        }
        click(view, rxClickEvent);
    }

    /**
     * @param view
     * @since 3.0.0
     */
    public void unclick(View view) {
        Disposable clickDisposable = mClickDisposables.get(view);
        if (clickDisposable != null) {
            clickDisposable.dispose();
        }
    }

    /**
     * Response data change
     * @since 3.0.0
     */
    public Consumer<JSONObject> asUpdateConsumer() {
        return new Consumer<JSONObject>() {
            @Override
            public void accept(JSONObject jsonObject) throws Exception {
                extras = jsonObject;
                mUpdateCellOpObservable.onNext(new UpdateCellOp(BaseCell.this));
            }
        };
    }

}
