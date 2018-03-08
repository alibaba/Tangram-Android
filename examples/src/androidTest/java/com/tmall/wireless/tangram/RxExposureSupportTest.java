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
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.support.ExposureCancellable;
import com.tmall.wireless.tangram.support.ExposureSupport;
import com.tmall.wireless.tangram.support.TangramRxEvent;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
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
        ExposureCancellable consumer1 = new ExposureCancellable() {
            @Override
            public void accept(TangramRxEvent tangramEvent) throws Exception {
                assertTrue(Looper.myLooper() == Looper.getMainLooper());
                assertEquals(tangramEvent.getView(), mView1);
                assertEquals(tangramEvent.getCell(), mBaseCell1);
                assertEquals(tangramEvent.getEventType(), 10);
                Log.d("RxExposureSupportTest", "testOneCellExposure test One cell mEventType " + tangramEvent.getEventType());
            }

            @Override
            public void cancel() throws Exception {
                super.cancel();
            }
        };
        mExposureSupport.setRxExposureCancellable(consumer1);
        mBaseCell1.exposure(mView1);
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testOneCellWithMultiViewExposure() {
        ExposureCancellable consumer = new ExposureCancellable() {

            @Override
            public void accept(TangramRxEvent tangramEvent) throws Exception {
                assertTrue(Looper.myLooper() == Looper.getMainLooper());
                assertTrue(tangramEvent.getView() == mView1 || tangramEvent.getView() == mView2);
                assertTrue(tangramEvent.getCell() == mBaseCell1);
                Log.d("RxExposureSupportTest", "testOneCellWithMultiViewExposure mEventType " + tangramEvent.getEventType());
                Log.d("RxExposureSupportTest", "testOneCellWithMultiViewExposure view " + tangramEvent.getView());
            }
        };
        mExposureSupport.setRxExposureCancellable(consumer);

        mBaseCell1.exposure(mView1);
        mBaseCell1.exposure(mView2);
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testExpsoureTaskAsync() {
        ExposureSupport exposureSupport = new ExposureSupport() {

            @Override
            public void onExposure(@NonNull Card card, int offset, int position) {

            }

            public Disposable onRxExposure(Observable<TangramRxEvent> exposureEventObservable, TangramRxEvent rxEvent) {
                return exposureEventObservable.subscribeOn(Schedulers.newThread()).subscribe(mCancellable);
            }

        };
        mServiceManager.register(ExposureSupport.class, exposureSupport);

        ExposureCancellable exposureCancellable = new ExposureCancellable() {

            @Override
            public void accept(TangramRxEvent tangramEvent) throws Exception {
                assertTrue(Looper.myLooper() != Looper.getMainLooper());
                assertEquals(tangramEvent.getView(), mView1);
                assertEquals(tangramEvent.getCell(), mBaseCell1);
                assertEquals(tangramEvent.getEventType(), 10);
                Log.d("RxExposureSupportTest", "testExpsoureTaskAsync  mEventType " + tangramEvent.getEventType());
                Log.d("RxExposureSupportTest",
                    "testExpsoureTaskAsync  thread " + Thread.currentThread().getId() + ": " + Thread.currentThread()
                        .getName());
            }

            @Override
            public void cancel() throws Exception {
                super.cancel();
            }
        };
        exposureSupport.setRxExposureCancellable(exposureCancellable);
        mBaseCell1.exposure(mView1);
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testExposureTaskAsyncThenCancelIt() {
        ExposureSupport exposureSupport = new ExposureSupport() {

            @Override
            public void onExposure(@NonNull Card card, int offset, int position) {

            }

            public Disposable onRxExposure(Observable<TangramRxEvent> exposureEventObservable, TangramRxEvent rxEvent) {
                return exposureEventObservable.subscribeOn(Schedulers.newThread()).delay(500, TimeUnit.MILLISECONDS)
                    .subscribe(mCancellable);
            }

        };
        mServiceManager.register(ExposureSupport.class, exposureSupport);

        ExposureCancellable exposureCancellable = new ExposureCancellable() {

            boolean cancel = false;

            @Override
            public void accept(TangramRxEvent tangramEvent) throws Exception {
                assertTrue(Looper.myLooper() != Looper.getMainLooper());
                //should not run here
                assertTrue(false);
                if (!cancel) {
                    assertEquals(tangramEvent.getView(), mView1);
                    assertEquals(tangramEvent.getCell(), mBaseCell1);
                    assertEquals(tangramEvent.getEventType(), 10);
                    Log.d("RxExposureSupportTest", "testExposureTaskAsyncThenCancelIt  mEventType " + tangramEvent.getEventType());
                    Log.d("RxExposureSupportTest",
                        "testExposureTaskAsyncThenCancelIt  thread " + Thread.currentThread().getId() + ": " + Thread.currentThread()
                            .getName());
                }
            }

            @Override
            public void cancel() throws Exception {
                super.cancel();
                cancel = true;
            }
        };
        exposureSupport.setRxExposureCancellable(exposureCancellable);
        mBaseCell1.exposure(mView1);
        mBaseCell1.unexposure(mView1);
    }

}
