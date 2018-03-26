package com.tmall.wireless.tangram.support;

import com.tmall.wireless.tangram.TangramEngine;
import com.tmall.wireless.tangram.op.UpdateCellOp;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by longerian on 2018/3/26.
 *
 * @author longerian
 * @date 2018/03/26
 */

public class RxTangramSupport {

    private CompositeDisposable mCompositeDisposable;
    private final TangramEngine mTangramEngine;

    public RxTangramSupport(TangramEngine tangramEngine) {
        mTangramEngine = tangramEngine;
        mCompositeDisposable = new CompositeDisposable();
    }

    public void observeCell(Observable<UpdateCellOp> observable) {
        mCompositeDisposable.add(observable.subscribe(mTangramEngine.asUpdateCellConsumer()));
    }

    public void destroy() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }

}
