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
import com.tmall.wireless.tangram.op.ClickExposureCellOp;
import com.tmall.wireless.tangram.structure.BaseCell;
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
        Consumer<ClickExposureCellOp> consumer1 = new Consumer<ClickExposureCellOp>() {
            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertEquals(clickEvent.getArg1(), mView1);
                assertEquals(clickEvent.getArg2(), mBaseCell1);
                assertEquals(clickEvent.getArg3().intValue(), 10);
                Log.d("RxClickSupportTest", "testOneCellClicks test One cell mEventType " + clickEvent.getArg3());
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
        Consumer<ClickExposureCellOp> consumer1 = new Consumer<ClickExposureCellOp>() {

            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertEquals(clickEvent.getArg1(), mView1);
                assertEquals(clickEvent.getArg2(), mBaseCell1);
                assertEquals(clickEvent.getArg3().intValue(), 10);
                Log.d("RxClickSupportTest", "testTwoCellClicks mEventType " + clickEvent.getArg3());
            }
        };
        mSimpleClickSupport.setConsumer(consumer1);

        mBaseCell1.click(mView1);
        mView1.performClick();

        Consumer<ClickExposureCellOp> consumer2 = new Consumer<ClickExposureCellOp>() {

            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertEquals(clickEvent.getArg1(), mView2);
                assertEquals(clickEvent.getArg2(), mBaseCell2);
                assertEquals(clickEvent.getArg3().intValue(), 1);
                Log.d("RxClickSupportTest", "testTwoCellClicks mEventType " + clickEvent.getArg3());
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
        Consumer<ClickExposureCellOp> consumer = new Consumer<ClickExposureCellOp>() {

            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertTrue(clickEvent.getArg1() == mView1 || clickEvent.getArg1() == mView2);
                assertTrue(clickEvent.getArg2() == mBaseCell1 || clickEvent.getArg2() == mBaseCell2);
                Log.d("RxClickSupportTest", "testOneConsumerSubscribeTwoCellClicks mEventType " + clickEvent.getArg3());
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
        Consumer<ClickExposureCellOp> consumer = new Consumer<ClickExposureCellOp>() {

            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                //should not execute this code
                assertTrue(false);
            }
        };
        mSimpleClickSupport.setConsumer(consumer);

        mBaseCell1.click(mView1);
        mSimpleClickSupport.destroy();
        mView1.performClick();
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testCellClickDisposeAndResubscribe() {
        Consumer<ClickExposureCellOp> consumer = new Consumer<ClickExposureCellOp>() {

            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertTrue(clickEvent.getArg1() == mView1);
                Log.d("RxClickSupportTest", "testCellClickDisposeAndResubscribe mEventType " + clickEvent.getArg3());
            }
        };
        mSimpleClickSupport.setConsumer(consumer);

        mBaseCell1.click(mView1);
        mBaseCell1.click(mView1);
        mView1.performClick();
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testOneCellWithMultiViewClick() {
        Consumer<ClickExposureCellOp> consumer = new Consumer<ClickExposureCellOp>() {

            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertTrue(clickEvent.getArg1() == mView1 || clickEvent.getArg1() == mView2);
                assertTrue(clickEvent.getArg2() == mBaseCell1);
                Log.d("RxClickSupportTest", "testOneCellWithMultiViewClick mEventType " + clickEvent.getArg3());
                Log.d("RxClickSupportTest", "testOneCellWithMultiViewClick view " + clickEvent.getArg1());
            }
        };
        mSimpleClickSupport.setConsumer(consumer);

        mBaseCell1.click(mView1);
        mBaseCell1.click(mView2);
        mView1.performClick();
        mView2.performClick();
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testOneCellWithMultiViewClickDispose() {
        Consumer<ClickExposureCellOp> consumer = new Consumer<ClickExposureCellOp>() {

            @Override
            public void accept(ClickExposureCellOp clickEvent) throws Exception {
                assertTrue(clickEvent.getArg1() == mView1 || clickEvent.getArg1() == mView2);
                assertTrue(clickEvent.getArg2() == mBaseCell1);
                Log.d("RxClickSupportTest", "testOneCellWithMultiViewClickDispose mEventType " + clickEvent.getArg3());
                Log.d("RxClickSupportTest", "testOneCellWithMultiViewClickDispose view " + clickEvent.getArg1());

                //should not execute this code
                assertTrue(false);
            }
        };
        mSimpleClickSupport.setConsumer(consumer);

        mBaseCell1.click(mView1);
        mBaseCell1.click(mView2);

        mSimpleClickSupport.destroy();

        mView1.performClick();
        mView2.performClick();
    }

}
