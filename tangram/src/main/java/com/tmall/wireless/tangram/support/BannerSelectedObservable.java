package com.tmall.wireless.tangram.support;

import com.tmall.wireless.tangram.view.BannerViewPager;
import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by longerian on 2018/3/9.
 *
 * @author longerian
 * @date 2018/03/09
 */

public class BannerSelectedObservable extends Observable<Integer> {

    private BannerViewPager mBannerViewPager;

    public BannerSelectedObservable(BannerViewPager bannerViewPager) {
        mBannerViewPager = bannerViewPager;
    }

    public void setBannerViewPager(BannerViewPager bannerViewPager) {
        mBannerViewPager = bannerViewPager;
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        RxBannerSelectedListener listener = new RxBannerSelectedListener(mBannerViewPager, observer);
        observer.onSubscribe(listener);
        mBannerViewPager.addOnPageChangeListener(listener);
    }
}
