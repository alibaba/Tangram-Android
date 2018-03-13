package com.tmall.wireless.tangram.example.support;

import android.util.Log;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.support.BannerSelectedObservable;
import com.tmall.wireless.tangram.support.BannerSupport;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by longerian on 2018/3/13.
 *
 * @author longerian
 * @date 2018/03/13
 */

public class SampleBannerSupport extends BannerSupport {

    @Override
    public Disposable onBannerSelectedObservable(Card banner, BannerSelectedObservable bannerSelectedObservable) {
        return bannerSelectedObservable.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d("Longer", "selected " + integer);
            }
        });
    }
}
