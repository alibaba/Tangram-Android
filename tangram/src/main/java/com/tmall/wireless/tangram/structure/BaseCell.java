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

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import com.tmall.wireless.tangram.Engine;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.ComponentLifecycle;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.op.ClickExposureCellOp;
import com.tmall.wireless.tangram.op.UpdateCellOp;
import com.tmall.wireless.tangram.support.CellClickObservable;
import com.tmall.wireless.tangram.support.CellExposureObservable;
import com.tmall.wireless.tangram.support.ExposureSupport;
import com.tmall.wireless.tangram.support.RxTangramSupport;
import com.tmall.wireless.tangram.support.SimpleClickSupport;
import com.tmall.wireless.tangram.util.BDE;
import com.tmall.wireless.tangram.util.IInnerImageSetter;
import com.tmall.wireless.tangram.util.ImageUtils;
import com.tmall.wireless.tangram.util.LifeCycleHelper;
import com.tmall.wireless.tangram.util.LifeCycleProviderImpl;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by mikeafc on 16/4/25.
 */
public class BaseCell<V extends View> extends ComponentLifecycle implements View.OnClickListener {

    public static final BaseCell NaN = new NanBaseCell();

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

    private ArrayMap<View, ClickExposureCellOp> mRxExposureEvents = new ArrayMap<>();

    private ArrayMap<View, CellExposureObservable> mViewExposureObservables = new ArrayMap<>();

    /**
     * @param targetView
     * @param rxClickExposureEvent
     * @since 3.0.0
     */
    public void exposure(View targetView, ClickExposureCellOp rxClickExposureEvent) {
        final ExposureSupport exposureSupport = serviceManager.getService(ExposureSupport.class);
        CellExposureObservable cellExposureObservable = mViewExposureObservables.get(targetView);
        if (cellExposureObservable == null) {
            cellExposureObservable = new CellExposureObservable(rxClickExposureEvent);
            mViewExposureObservables.put(targetView, cellExposureObservable);
        } else {
            cellExposureObservable.setRxClickExposureEvent(rxClickExposureEvent);
        }
        if (exposureSupport != null) {
            exposureSupport.onRxExposure(cellExposureObservable, rxClickExposureEvent);
        }
    }

    /**
     * @param targetView
     * @since 3.0.0
     */
    public void exposure(View targetView) {
        ClickExposureCellOp rxExposureEvent = mRxExposureEvents.get(targetView);
        if (rxExposureEvent == null) {
            rxExposureEvent = new ClickExposureCellOp(targetView, this, this.pos);
            mRxExposureEvents.put(targetView, rxExposureEvent);
        } else {
            rxExposureEvent.setArg1(targetView);
            rxExposureEvent.setArg2(this);
            rxExposureEvent.setArg3(this.pos);
        }
        exposure(targetView, rxExposureEvent);
    }

    private ArrayMap<View, ClickExposureCellOp> mRxClickEvents = new ArrayMap<>();

    private ArrayMap<View, CellClickObservable> mViewClickObservables = new ArrayMap<>();

    /**
     * @param view
     * @param rxClickExposureEvent
     * @since 3.0.0
     */
    public void click(View view, ClickExposureCellOp rxClickExposureEvent) {
        CellClickObservable cellClickObservable = mViewClickObservables.get(view);
        if (cellClickObservable == null) {
            cellClickObservable = new CellClickObservable(rxClickExposureEvent);
            mViewClickObservables.put(view, cellClickObservable);
        } else {
            cellClickObservable.setRxClickExposureEvent(rxClickExposureEvent);
        }
        if (serviceManager != null) {
            final SimpleClickSupport service = serviceManager.getService(SimpleClickSupport.class);
            if (service != null) {
                service.onRxClick(cellClickObservable, rxClickExposureEvent);
            }
        }
    }

    /**
     * @param view
     * @since 3.0.0
     */
    public void click(View view) {
        ClickExposureCellOp rxClickEvent = mRxClickEvents.get(view);
        if (rxClickEvent == null) {
            rxClickEvent = new ClickExposureCellOp(view, this, this.pos);
            mRxClickEvents.put(view, rxClickEvent);
        } else {
            rxClickEvent.setArg1(view);
            rxClickEvent.setArg2(this);
            rxClickEvent.setArg3(this.pos);
        }
        click(view, rxClickEvent);
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

    public static final class NanBaseCell extends BaseCell {
        @Override
        public boolean isValid() {
            return false;
        }
    }

    private LifeCycleProviderImpl<BDE> mLifeCycleProvider;


    public void emitNext(BDE event) {
        if (mLifeCycleProvider == null) {
            mLifeCycleProvider = new LifeCycleProviderImpl<>();
        }
        mLifeCycleProvider.emitNext(event);
    }

    public LifeCycleProviderImpl<BDE> getLifeCycleProvider() {
        return mLifeCycleProvider;
    }



}
