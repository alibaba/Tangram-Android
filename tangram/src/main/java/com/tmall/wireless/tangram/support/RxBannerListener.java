package com.tmall.wireless.tangram.support;

import java.util.ArrayList;
import java.util.List;

import com.tmall.wireless.tangram.ext.BannerListener;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * Created by longerian on 2018/3/9.
 *
 * @author longerian
 * @date 2018/03/09
 */

public abstract class RxBannerListener<T> extends MainThreadDisposable implements BannerListener {

    protected List<Observer<? super T>> mObservers = new ArrayList<>();

    public void setObserver(Observer<? super T> observer) {
        mObservers.add(observer);
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels, int direction) {

    }

    public void onPageSelected(int position) {

    }

    public void onPageScrollStateChanged(int state) {

    }

    public void onItemPositionInBanner(int position) {

    }

    @Override
    protected void onDispose() {

    }
}
