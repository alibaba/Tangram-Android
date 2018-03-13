package com.tmall.wireless.tangram.support;

import java.util.ArrayList;
import java.util.List;

import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.ext.BannerListener;
import io.reactivex.disposables.Disposable;

/**
 * You can get banner scroll info from this support
 * Created by mikeafc on 17/4/9.
 */
//FIXME handle multi banners
public class BannerSupport {
    private List<BannerListener> listeners = new ArrayList<BannerListener>();

    public void registerPageChangeListener(BannerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterPageChangeListener(BannerListener listener) {
        listeners.remove(listener);
    }

    public List<BannerListener> getListeners() {
        return listeners;
    }

    public Disposable onBannerSelectedObservable(Card banner, BannerSelectedObservable bannerSelectedObservable) {
        return null;
    }
}