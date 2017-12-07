package com.tmall.wireless.tangram.support;

import java.util.ArrayList;
import java.util.List;

import com.tmall.wireless.tangram.ext.BannerListener;

/**
 * You can get banner scroll info from this support
 * Created by mikeafc on 17/4/9.
 */

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
}