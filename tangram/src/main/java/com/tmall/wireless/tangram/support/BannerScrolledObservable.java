package com.tmall.wireless.tangram.support;

import com.tmall.wireless.tangram.support.RxBannerScrolledListener.ScrollEvent;
import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by longerian on 2018/3/14.
 *
 * @author longerian
 * @date 2018/03/14
 */

public class BannerScrolledObservable extends Observable<ScrollEvent> {

    private final RxBannerListener mBannerListener;

    public BannerScrolledObservable(RxBannerScrolledListener bannerListener) {
        mBannerListener = bannerListener;
    }

    @Override
    protected void subscribeActual(Observer<? super ScrollEvent> observer) {
        observer.onSubscribe(mBannerListener);
        mBannerListener.setObserver(observer);
    }

}
