package com.tmall.wireless.tangram.support;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Consumer;

/**
 * Created by longerian on 2018/3/9.
 *
 * @author longerian
 * @date 2018/03/09
 */

public class RxBannerSelectedListener extends RxBannerListener<Integer> {

    @Override
    public void onPageSelected(final int position) {
        if (!isDisposed()) {
            Observable.fromIterable(mObservers).subscribe(new Consumer<Observer<? super Integer>>() {
                @Override
                public void accept(Observer<? super Integer> observer) throws Exception {
                    observer.onNext(position);
                }
            });
        }
    }

}
