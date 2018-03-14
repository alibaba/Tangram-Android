package com.tmall.wireless.tangram.support;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.util.ArrayMap;
import com.tmall.wireless.tangram.ext.BannerListener;
import com.tmall.wireless.tangram.support.RxBannerScrolledListener.ScrollEvent;
import io.reactivex.Observable;

/**
 * You can get banner scroll info from this support
 * Created by mikeafc on 17/4/9.
 */
public class BannerSupport {

    private ArrayMap<String, BannerListener> mSelectedListenerArrayMap = new ArrayMap<>();
    private ArrayMap<String, BannerSelectedObservable> mSelectedObservableArrayMap = new ArrayMap<>();

    private ArrayMap<String, BannerListener> mScrolledListenerArrayMap = new ArrayMap<>();
    private ArrayMap<String, BannerScrolledObservable> mScrolledObservableArrayMap = new ArrayMap<>();

    private ArrayMap<String, BannerListener> mScrollStateListenerArrayMap = new ArrayMap<>();
    private ArrayMap<String, BannerScrollStateChangedObservable> mScrollStateObservableArrayMap = new ArrayMap<>();

    @Deprecated
    private List<BannerListener> listeners = new ArrayList<BannerListener>();

    @Deprecated
    public void registerPageChangeListener(BannerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Deprecated
    public void unregisterPageChangeListener(BannerListener listener) {
        listeners.remove(listener);
    }

    @Deprecated
    public List<BannerListener> getListeners() {
        return listeners;
    }

    public BannerListener getSelectedListenerById(String id) {
        return mSelectedListenerArrayMap.get(id);
    }

    public BannerListener getScrolledListenerById(String id) {
        return mScrolledListenerArrayMap.get(id);
    }

    public BannerListener getScrollStateChangedListenerById(String id) {
        return mScrollStateListenerArrayMap.get(id);
    }

    public Observable<Integer> observeSelected(String id) {
        BannerListener rxBannerSelectedListener = mSelectedListenerArrayMap.get(id);
        if (rxBannerSelectedListener == null) {
            rxBannerSelectedListener = new RxBannerSelectedListener();
            mSelectedListenerArrayMap.put(id, rxBannerSelectedListener);
        }
        BannerSelectedObservable bannerSupportObservable = mSelectedObservableArrayMap.get(id);
        if (bannerSupportObservable == null) {
            bannerSupportObservable = new BannerSelectedObservable((RxBannerSelectedListener)rxBannerSelectedListener);
            bannerSupportObservable.share();
        }
        return bannerSupportObservable;
    }

    public Observable<ScrollEvent> observeScrolled(String id) {
        BannerListener rxBannerSelectedListener = mScrolledListenerArrayMap.get(id);
        if (rxBannerSelectedListener == null) {
            rxBannerSelectedListener = new RxBannerScrolledListener();
            mScrolledListenerArrayMap.put(id, rxBannerSelectedListener);
        }
        BannerScrolledObservable bannerSupportObservable = mScrolledObservableArrayMap.get(id);
        if (bannerSupportObservable == null) {
            bannerSupportObservable = new BannerScrolledObservable((RxBannerScrolledListener)rxBannerSelectedListener);
            bannerSupportObservable.share();
        }
        return bannerSupportObservable;
    }

    public Observable<Integer> observeScrollStateChanged(String id) {
        BannerListener rxBannerSelectedListener = mScrollStateListenerArrayMap.get(id);
        if (rxBannerSelectedListener == null) {
            rxBannerSelectedListener = new RxBannerScrollStateChangedListener();
            mScrollStateListenerArrayMap.put(id, rxBannerSelectedListener);
        }
        BannerScrollStateChangedObservable bannerSupportObservable = mScrollStateObservableArrayMap.get(id);
        if (bannerSupportObservable == null) {
            bannerSupportObservable = new BannerScrollStateChangedObservable((RxBannerScrollStateChangedListener)rxBannerSelectedListener);
            bannerSupportObservable.share();
        }
        return bannerSupportObservable;
    }

    public void destroy() {
        destroyBannerSelected();
        destroyBannerScrolled();
        destroyBannerScrollStateChanged();
    }

    public void destroyBannerSelected() {
        mSelectedListenerArrayMap.clear();
        mSelectedObservableArrayMap.clear();

    }

    public void destroyBannerScrolled() {
        mScrolledListenerArrayMap.clear();
        mScrolledObservableArrayMap.clear();

    }

    public void destroyBannerScrollStateChanged() {
        mScrollStateListenerArrayMap.clear();
        mScrollStateObservableArrayMap.clear();
    }

}