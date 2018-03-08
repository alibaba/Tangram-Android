package com.tmall.wireless.tangram.support;

import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;

/**
 * Created by longerian on 2018/3/7.
 *
 * @author longerian
 * @date 2018/03/07
 */

public abstract class RxExposureCancellable implements Consumer<TangramRxEvent>, Cancellable {

    public RxExposureCancellable() {
    }

}