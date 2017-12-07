package com.tmall.wireless.tangram.ext;

/**
 * Created by mikeafc on 17/4/10.
 */

public interface BannerListener {
    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels, int direction);

    void onPageSelected(int position);

    void onPageScrollStateChanged(int state);

    void onItemPositionInBanner(int position);
}