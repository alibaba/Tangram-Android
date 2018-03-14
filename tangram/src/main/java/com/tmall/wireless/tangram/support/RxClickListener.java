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
