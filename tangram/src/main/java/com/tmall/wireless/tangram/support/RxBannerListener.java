package com.tmall.wireless.tangram.support;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.tmall.wireless.tangram.view.BannerViewPager;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * Created by longerian on 2018/3/9.
 *
 * @author longerian
 * @date 2018/03/09
 */

public abstract class RxBannerListener<T> extends MainThreadDisposable implements OnPageChangeListener {

    protected final BannerViewPager mBannerViewPager;
    protected final Observer<? super T> mObserver;

    public RxBannerListener(BannerViewPager view, Observer<? super T> observer) {
        this.mBannerViewPager = view;
        this.mObserver = observer;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onDispose() {
        mBannerViewPager.removeOnPageChangeListener(this);
    }


}
