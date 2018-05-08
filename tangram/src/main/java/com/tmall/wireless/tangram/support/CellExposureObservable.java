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

import java.util.concurrent.atomic.AtomicBoolean;

import com.tmall.wireless.tangram.op.ClickExposureCellOp;
import com.tmall.wireless.tangram.util.Preconditions;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.CancellableDisposable;

/**
 * Created by longerian on 2018/3/7.
 *
 * @author longerian
 * @date 2018/03/07
 */

public class CellExposureObservable extends Observable<ClickExposureCellOp> {

    private ClickExposureCellOp mRxClickExposureEvent;

    private ExposureDispose mExposureDispose;

    public CellExposureObservable(ClickExposureCellOp rxClickExposureEvent) {
        Preconditions.checkNotNull(rxClickExposureEvent);
        Preconditions.checkNotNull(rxClickExposureEvent.getArg1());
        this.mRxClickExposureEvent = rxClickExposureEvent;
    }

    public void setRxClickExposureEvent(ClickExposureCellOp rxClickExposureEvent) {
        mRxClickExposureEvent = rxClickExposureEvent;
    }

    @Override
    protected void subscribeActual(Observer<? super ClickExposureCellOp> observer) {
        if (mExposureDispose == null) {
            mExposureDispose = new ExposureDispose();
        }
        observer.onSubscribe(mExposureDispose);
        if (!mExposureDispose.isDisposed()) {
            observer.onNext(mRxClickExposureEvent);
        }
    }

    private static class ExposureDispose implements Disposable {

        private final AtomicBoolean unsubscribed = new AtomicBoolean();

        @Override
        public void dispose() {
            unsubscribed.compareAndSet(false, true);
        }

        @Override
        public boolean isDisposed() {
            return unsubscribed.get();
        }
    }


}
