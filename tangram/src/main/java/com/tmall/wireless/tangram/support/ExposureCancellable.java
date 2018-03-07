package com.tmall.wireless.tangram.support;

import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;

/**
 * Created by longerian on 2018/3/7.
 *
 * @author longerian
 * @date 2018/03/07
 */

public class ExposureCancellable implements Consumer<TangramRxEvent>, Cancellable {

    public ExposureCancellable() {
    }

    @Override
    public void cancel() throws Exception {
        //TODO async task can be canelled
    }

    @Override
    public void accept(TangramRxEvent o) throws Exception {
        //TODO do exposure task async
    }
}