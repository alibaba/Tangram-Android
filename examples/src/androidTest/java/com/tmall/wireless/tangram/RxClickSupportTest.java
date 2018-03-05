package com.tmall.wireless.tangram;

import java.util.Map;

import android.content.Context;
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
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.support.ClickEvent;
import com.tmall.wireless.tangram.support.SimpleClickSupport;
import io.reactivex.functions.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by longerian on 2018/3/5.
 *
 * @author longerian
 * @date 2018/03/05
 */
@RunWith(AndroidJUnit4.class)
public class RxClickSupportTest extends AndroidTestCase {

    @Rule
    public final UiThreadTestRule uiThread = new UiThreadTestRule();

    private final Context context = InstrumentationRegistry.getContext();
    private final View mView1 = new View(context);
    private final View mView2 = new View(context);
    private final BaseCell mBaseCell1 = new BaseCell();
    private final BaseCell mBaseCell2 = new BaseCell();
    private final SimpleClickSupport mSimpleClickSupport = new SimpleClickSupport() {

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
    public void setUp() {
        mServiceManager.register(SimpleClickSupport.class, mSimpleClickSupport);

        mBaseCell1.pos = 10;
        mBaseCell1.serviceManager = mServiceManager;

        mBaseCell2.pos = 1;
        mBaseCell2.serviceManager = mServiceManager;
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testOneCellClicks() {
        Consumer<ClickEvent> consumer1 = new Consumer<ClickEvent>() {
            @Override
            public void accept(ClickEvent clickEvent) throws Exception {
                assertEquals(clickEvent.mView, mView1);
                assertEquals(clickEvent.mCell, mBaseCell1);
                assertEquals(clickEvent.eventType, 10);
                Log.d("RxClickSupportTest", "test One cell eventType " + clickEvent.eventType);
            }
        };
        mSimpleClickSupport.setConsumer(consumer1);
        mBaseCell1.click(mView1);
        mView1.performClick();
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testTwoCellClicks() {
        Consumer<ClickEvent> consumer1 = new Consumer<ClickEvent>() {

            @Override
            public void accept(ClickEvent clickEvent) throws Exception {
                assertEquals(clickEvent.mView, mView1);
                assertEquals(clickEvent.mCell, mBaseCell1);
                assertEquals(clickEvent.eventType, 10);
                Log.d("RxClickSupportTest", "eventType " + clickEvent.eventType);
            }
        };
        mSimpleClickSupport.setConsumer(consumer1);

        mBaseCell1.click(mView1);
        mView1.performClick();

        Consumer<ClickEvent> consumer2 = new Consumer<ClickEvent>() {

            @Override
            public void accept(ClickEvent clickEvent) throws Exception {
                assertEquals(clickEvent.mView, mView2);
                assertEquals(clickEvent.mCell, mBaseCell2);
                assertEquals(clickEvent.eventType, 1);
                Log.d("RxClickSupportTest", "eventType " + clickEvent.eventType);
            }
        };
        mSimpleClickSupport.setConsumer(consumer2);

        mBaseCell2.click(mView2);
        mView2.performClick();

    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testOneConsumerSubscribeTwoCellClicks() {
        Consumer<ClickEvent> consumer = new Consumer<ClickEvent>() {

            @Override
            public void accept(ClickEvent clickEvent) throws Exception {
                assertTrue(clickEvent.mView == mView1 || clickEvent.mView == mView2);
                assertTrue(clickEvent.mCell == mBaseCell1 || clickEvent.mCell == mBaseCell2);
                Log.d("RxClickSupportTest", "eventType " + clickEvent.eventType);
            }
        };
        mSimpleClickSupport.setConsumer(consumer);

        mBaseCell1.click(mView1);
        mView1.performClick();

        mBaseCell2.click(mView2);
        mView2.performClick();

    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testCellClickDispose() {
        Consumer<ClickEvent> consumer = new Consumer<ClickEvent>() {

            @Override
            public void accept(ClickEvent clickEvent) throws Exception {
                //should not execute this code
                assertTrue(false);
            }
        };
        mSimpleClickSupport.setConsumer(consumer);

        mBaseCell1.click(mView1);
        mBaseCell1.unclick();
    }

}
