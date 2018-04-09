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

package com.tmall.wireless.tangram;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.SmallTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.util.ArrayMap;
import android.test.AndroidTestCase;
import android.util.Log;
import android.view.View;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.op.ClickExposureCellOp;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.support.ExposureSupport;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.operators.maybe.MaybeCallbackObserver;
import io.reactivex.schedulers.Schedulers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by longerian on 2018/3/7.
 *
 * @author longerian
 * @date 2018/03/07
 */

@RunWith(AndroidJUnit4.class)
public class RxExposureSupportTest extends AndroidTestCase {

    @Rule
    public final UiThreadTestRule uiThread = new UiThreadTestRule();

    private final Context context = InstrumentationRegistry.getContext();
    private final View mView1 = new View(context);
    private final View mView2 = new View(context);
    private final BaseCell mBaseCell1 = new BaseCell();
    private final BaseCell mBaseCell2 = new BaseCell();
    private final ExposureSupport mExposureSupport = new ExposureSupport() {

        @Override
        public void onExposure(@NonNull Card card, int offset, int position) {

        }
    };


    private final ServiceManager mServiceManager = new ServiceManager() {
        private Map<Class<?>, Object> mServices = new ArrayMap<>();

        @Override
        public <T> void register(Class<T> type, T service) {
            mServices.put(type, type.cast(service));
        }

        @Override
        public <T> T getService(Class<T> type) {
            Object service = mServices.get(type);
            if (service == null) {
                return null;
            }
            return type.cast(service);
        }
        @Override
        public boolean supportRx() {
            return true;
        }
    };

    @Before
    public void setUp() throws Exception {
        mServiceManager.register(ExposureSupport.class, mExposureSupport);

        mBaseCell1.pos = 10;
        mBaseCell1.serviceManager = mServiceManager;

        mBaseCell2.pos = 1;
        mBaseCell2.serviceManager = mServiceManager;
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testOneCellExposure() {
        Consumer<ClickExposureCellOp> consumer1 = new Consumer<ClickExposureCellOp>() {
            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertEquals(clickEvent.getArg1(), mView1);
                assertEquals(clickEvent.getArg2(), mBaseCell1);
                assertEquals(clickEvent.getArg3().intValue(), 10);
                Log.d("RxExposureSupportTest", "testOneCellExposure test One cell mEventType " + clickEvent.getArg3());
            }
        };
        mExposureSupport.setConsumer(consumer1);
        mBaseCell1.exposure(mView1);
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testOneCellWithMultiViewExposure() {
        Consumer<ClickExposureCellOp> consumer1 = new Consumer<ClickExposureCellOp>() {
            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertTrue(Looper.myLooper() == Looper.getMainLooper());
                assertTrue(clickEvent.getArg1() == mView1 || clickEvent.getArg1() == mView2);
                assertTrue(clickEvent.getArg2() == mBaseCell1);
                Log.d("RxExposureSupportTest", "testOneCellWithMultiViewExposure mEventType " + clickEvent.getArg3());
                Log.d("RxExposureSupportTest", "testOneCellWithMultiViewExposure view " + clickEvent.getArg1());
            }
        };
        mExposureSupport.setConsumer(consumer1);

        mBaseCell1.exposure(mView1);
        mBaseCell1.exposure(mView2);
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testOneConsumerSubscribeTwoCellExposure() {
        Consumer<ClickExposureCellOp> consumer = new Consumer<ClickExposureCellOp>() {

            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertTrue(clickEvent.getArg1() == mView1 || clickEvent.getArg1() == mView2);
                assertTrue(clickEvent.getArg2() == mBaseCell1 || clickEvent.getArg2() == mBaseCell2);
                Log.d("RxExposureSupportTest", "testOneConsumerSubscribeTwoCellExposure mEventType " + clickEvent.getArg3());
            }
        };
        mExposureSupport.setConsumer(consumer);
        mBaseCell1.exposure(mView1);
        mBaseCell2.exposure(mView2);
    }


    @Test
    @SmallTest
    @UiThreadTest
    public void testDestroyExposure() {
        ExposureSupport exposureSupport = new ExposureSupport() {

            private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

            Consumer<ClickExposureCellOp> consumer = new Consumer<ClickExposureCellOp>() {

                @Override
                public void accept(ClickExposureCellOp clickEvent) throws Exception {
                    //should not execute here
                    assertTrue(false);
                    assertTrue(Looper.myLooper() == Looper.getMainLooper());
                    assertEquals(clickEvent.getArg1(), mView1);
                    assertEquals(clickEvent.getArg2(), mBaseCell1);
                    assertEquals(clickEvent.getArg3().intValue(), 10);
                    Log.d("RxExposureSupportTest", "testExposureTaskAsyncThenCancelItButResubscribe  mEventType " + clickEvent.getArg3());
                    Log.d("RxExposureSupportTest",
                        "testDestroyExposure  thread " + Thread.currentThread().getId() + ": " + Thread.currentThread()
                            .getName());
                }
            };


            @Override
            public void onExposure(@NonNull Card card, int offset, int position) {

            }

            @Override
            public void onRxExposure(Observable<ClickExposureCellOp> exposureCellOpObservable,
                ClickExposureCellOp rxEvent) {
                mCompositeDisposable.add(
                    exposureCellOpObservable.delaySubscription(1, TimeUnit.MINUTES).subscribe(consumer));
            }

            @Override
            public void destroy() {
                super.destroy();
                mCompositeDisposable.clear();
            }
        };
        mServiceManager.register(ExposureSupport.class, exposureSupport);
        mBaseCell1.exposure(mView1);
        mBaseCell1.exposure(mView1);
        exposureSupport.destroy();

    }

}
