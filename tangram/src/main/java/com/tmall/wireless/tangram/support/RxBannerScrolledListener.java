/*
 * MIT License
 *
 * Copyright (c) 2018 Alibaba Group
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
