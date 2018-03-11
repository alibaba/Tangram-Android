package com.tmall.wireless.tangram.support;

import com.tmall.wireless.tangram.view.BannerViewPager;
import io.reactivex.Observer;

/**
 * Created by longerian on 2018/3/9.
 *
 * @author longerian
 * @date 2018/03/09
 */

public class RxBannerSelectedListener extends RxBannerListener<Integer> {

    public RxBannerSelectedListener(BannerViewPager view,
        Observer<? super Integer> observer) {
        super(view, observer);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (!isDisposed()) {
            mObserver.onNext(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
