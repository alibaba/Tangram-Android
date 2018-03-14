package com.tmall.wireless.tangram.support;

import com.tmall.wireless.tangram.support.RxBannerScrolledListener.ScrollEvent;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Consumer;

/**
 * Created by longerian on 2018/3/9.
 *
 * @author longerian
 * @date 2018/03/09
 */

public class RxBannerScrolledListener extends RxBannerListener<ScrollEvent> {

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels, int direction) {
        if (!isDisposed()) {
            Observable.fromIterable(mObservers).subscribe(new Consumer<Observer<? super ScrollEvent>>() {
                @Override
                public void accept(Observer<? super ScrollEvent> observer) throws Exception {
                    observer.onNext(new ScrollEvent(position, positionOffset, positionOffsetPixels));
                }
            });
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

        @Override
        public String toString() {
            return "ScrollEvent{" +
                "position=" + position +
                ", positionOffset=" + positionOffset +
                ", positionOffsetPixels=" + positionOffsetPixels +
                '}';
        }
    }

}
