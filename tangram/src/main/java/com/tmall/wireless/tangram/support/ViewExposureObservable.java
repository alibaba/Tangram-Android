package com.tmall.wireless.tangram.support;

import com.tmall.wireless.tangram.util.Preconditions;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.internal.disposables.CancellableDisposable;

/**
 * Created by longerian on 2018/3/7.
 *
 * @author longerian
 * @date 2018/03/07
 */

public class ViewExposureObservable extends Observable<TangramRxEvent> {

    private TangramRxEvent mTangramRxEvent;
    private RxExposureCancellable mRxExposureCancellable;

    public ViewExposureObservable(TangramRxEvent tangramRxEvent, RxExposureCancellable rxExposureCancellable) {
        Preconditions.checkNotNull(tangramRxEvent);
        Preconditions.checkNotNull(tangramRxEvent.getView());
        this.mTangramRxEvent = tangramRxEvent;
        this.mRxExposureCancellable = rxExposureCancellable;
    }

    public void setTangramRxEvent(TangramRxEvent tangramRxEvent) {
        mTangramRxEvent = tangramRxEvent;
    }

    public void setRxExposureCancellable(RxExposureCancellable rxExposureCancellable) {
        mRxExposureCancellable = rxExposureCancellable;
    }

    @Override
    protected void subscribeActual(Observer<? super TangramRxEvent> observer) {
        if (mRxExposureCancellable != null) {
            observer.onSubscribe(new CancellableDisposable(mRxExposureCancellable));
        }
        observer.onNext(mTangramRxEvent);
    }

}
