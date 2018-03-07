package com.tmall.wireless.tangram.support;

import com.tmall.wireless.tangram.util.Preconditions;
import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by longerian on 2018/3/7.
 *
 * @author longerian
 * @date 2018/03/07
 */

public class ViewClickObservable extends Observable<TangramRxEvent> {

    private TangramRxEvent mTangramRxEvent;

    private RxClickListener mListener;

    public ViewClickObservable(TangramRxEvent tangramRxEvent) {
        Preconditions.checkNotNull(tangramRxEvent);
        Preconditions.checkNotNull(tangramRxEvent.getView());
        this.mTangramRxEvent = tangramRxEvent;
    }

    public void setTangramRxEvent(TangramRxEvent tangramRxEvent) {
        mTangramRxEvent = tangramRxEvent;
    }

    @Override
    protected void subscribeActual(Observer<? super TangramRxEvent> observer) {
        if (!Preconditions.checkMainThread(observer)) {
            return;
        }
        if (mListener == null) {
            mListener = new RxClickListener(mTangramRxEvent, observer);
        } else {
            mListener.setTangramRxEvent(mTangramRxEvent);
            mListener.setObserver(observer);
        }
        observer.onSubscribe(mListener);
        mTangramRxEvent.getView().setOnClickListener(mListener);
    }

}
