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

    public ViewExposureObservable(TangramRxEvent tangramRxEvent) {
        Preconditions.checkNotNull(tangramRxEvent);
        Preconditions.checkNotNull(tangramRxEvent.getView());
        this.mTangramRxEvent = tangramRxEvent;
    }

    public void setTangramRxEvent(TangramRxEvent tangramRxEvent) {
        mTangramRxEvent = tangramRxEvent;
    }

    @Override
    protected void subscribeActual(Observer<? super TangramRxEvent> observer) {
        if (mTangramRxEvent.getCell().serviceManager != null) {
            final ExposureSupport service = mTangramRxEvent.getCell().serviceManager.getService(ExposureSupport.class);
            if (service != null) {
                observer.onSubscribe(new CancellableDisposable(service.getRxExposureCancellable(mTangramRxEvent)));
            }
        }
        observer.onNext(mTangramRxEvent);
    }

}
