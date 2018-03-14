package com.tmall.wireless.tangram.support;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by longerian on 2018/3/14.
 *
 * @author longerian
 * @date 2018/03/14
 */

public class BannerScrollStateChangedObservable extends Observable<Integer> {

    private final RxBannerListener mBannerListener;

    public BannerScrollStateChangedObservable(RxBannerScrollStateChangedListener bannerListener) {
        mBannerListener = bannerListener;
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        observer.onSubscribe(mBannerListener);
        mBannerListener.setObserver(observer);
    }
}
