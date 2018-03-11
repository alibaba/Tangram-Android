package com.tmall.wireless.tangram.support;

import com.tmall.wireless.tangram.support.RxBannerScrolledListener.ScrollEvent;
import com.tmall.wireless.tangram.view.BannerViewPager;
import io.reactivex.Observer;

/**
 * Created by longerian on 2018/3/9.
 *
 * @author longerian
 * @date 2018/03/09
 */

public class RxBannerScrolledListener extends RxBannerListener<ScrollEvent> {

    public RxBannerScrolledListener(BannerViewPager view,
        Observer<? super ScrollEvent> observer) {
        super(view, observer);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (!isDisposed()) {
            mObserver.onNext(new ScrollEvent(position, positionOffset, positionOffsetPixels));
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public static class ScrollEvent {

        public final int position;
        public final float positionOffset;
        public final int positionOffsetPixels;

        public ScrollEvent(int position, float positionOffset, int positionOffsetPixels) {
            this.position = position;
            this.positionOffset = positionOffset;
            this.positionOffsetPixels = positionOffsetPixels;
        }
    }

}
