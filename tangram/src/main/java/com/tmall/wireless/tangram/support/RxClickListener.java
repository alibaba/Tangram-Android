package com.tmall.wireless.tangram.support;

import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import android.view.View;

/**
 * Created by longerian on 2018/3/7.
 *
 * @author longerian
 * @date 2018/03/07
 */

public class RxClickListener extends MainThreadDisposable implements View.OnClickListener {

    private TangramRxEvent mTangramRxEvent;

    private Observer<? super TangramRxEvent> mObserver;

    public RxClickListener(TangramRxEvent tangramRxEvent, Observer<? super TangramRxEvent> observer) {
        this.mTangramRxEvent = tangramRxEvent;
        this.mObserver = observer;
    }

    public void setTangramRxEvent(TangramRxEvent tangramRxEvent) {
        mTangramRxEvent = tangramRxEvent;
    }

    public void setObserver(Observer<? super TangramRxEvent> observer) {
        mObserver = observer;
    }

    @Override
    public void onClick(View v) {
        if (!isDisposed()) {
            mObserver.onNext(mTangramRxEvent);
        }
    }

    @Override
    protected void onDispose() {
        mTangramRxEvent.getView().setOnClickListener(null);
    }

}
