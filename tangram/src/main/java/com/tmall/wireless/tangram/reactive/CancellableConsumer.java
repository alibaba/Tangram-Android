package com.tmall.wireless.tangram.reactive;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;

/**
 * Created by longerian on 2018/3/29.
 *
 * @author longerian
 * @date 2018/03/29
 */

public abstract class CancellableConsumer<T> implements Consumer<T>, Cancellable {

    private AtomicBoolean cancel = new AtomicBoolean();

    public CancellableConsumer() {
        this.cancel.set(false);
    }

    @Override
    final public void cancel() throws Exception {
        this.cancel.set(true);
    }

    @Override
    final public void accept(T t) throws Exception {
        if (!cancel.get()) {
            onAccept(t);
        }
    }

    public abstract void onAccept(T t) throws Exception;
}
