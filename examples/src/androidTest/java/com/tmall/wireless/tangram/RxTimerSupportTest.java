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
import android.os.Looper;
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
import com.tmall.wireless.tangram.support.ITimer.TimerStatus;
import com.tmall.wireless.tangram.support.TimerSupport;
import com.tmall.wireless.tangram.support.TimerSupport.OnTickListener;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by longerian on 2018/3/8.
 *
 * @author longerian
 * @date 2018/03/08
 */

@RunWith(AndroidJUnit4.class)
public class RxTimerSupportTest extends AndroidTestCase {

    @Rule
    public final UiThreadTestRule uiThread = new UiThreadTestRule();

    private final Context context = InstrumentationRegistry.getContext();
    private final View mView1 = new View(context);
    private final View mView2 = new View(context);
    private final BaseCell mBaseCell1 = new BaseCell();
    private final BaseCell mBaseCell2 = new BaseCell();

    private final TimerSupport mTimerSupport = new TimerSupport();

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
        mBaseCell1.pos = 10;
        mBaseCell1.serviceManager = mServiceManager;

        mBaseCell2.pos = 1;
        mBaseCell2.serviceManager = mServiceManager;

        mServiceManager.register(TimerSupport.class, mTimerSupport);
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testTimerExecution() {
        assertEquals(mTimerSupport.getStatus(), TimerStatus.Waiting);
        mTimerSupport.register(1, new OnTickListener() {
            long start = System.currentTimeMillis();

            @Override
            public void onTick() {
                assertEquals(Looper.myLooper(), Looper.getMainLooper());
                assertEquals(mTimerSupport.getStatus(), TimerStatus.Running);
                long end = System.currentTimeMillis();
                long time = (end - start);
                Log.d("RxTimerSupportTest", "testTimerExecution " + time);
                assertTrue(Math.abs(time - 1 * 1000) < 50);
                start = end;
            }
        });
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testTimerIntermediateArg() {
        assertEquals(mTimerSupport.getStatus(), TimerStatus.Waiting);
        mTimerSupport.register(1, new OnTickListener() {

            long start = System.currentTimeMillis();

            @Override
            public void onTick() {
                assertEquals(Looper.myLooper(), Looper.getMainLooper());
                assertEquals(mTimerSupport.getStatus(), TimerStatus.Running);
                long end = System.currentTimeMillis();
                long time = (end - start);
                Log.d("RxTimerSupportTest", "testTimerIntermediateArg " + time);
                assertTrue(Math.abs(time) < 50 || Math.abs(time - 1 * 1000) < 50);
                start = end;
            }
        }, true);

    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testRegisterAndUnregister() {
        assertEquals(mTimerSupport.getStatus(), TimerStatus.Waiting);
        OnTickListener onTickListener = new OnTickListener() {

            long start = System.currentTimeMillis();

            @Override
            public void onTick() {
                assertEquals(Looper.myLooper(), Looper.getMainLooper());
                assertEquals(mTimerSupport.getStatus(), TimerStatus.Running);
                long end = System.currentTimeMillis();
                long time = (end - start);
                Log.d("RxTimerSupportTest", "testTimerIntermediateArg " + time);
                //assertTrue(Math.abs(time) < 50 || Math.abs(time - 1 * 1000) < 50);
                start = end;
            }
        };
        mTimerSupport.register(1, onTickListener, true);
        mTimerSupport.unregister(onTickListener);
        mTimerSupport.register(1, onTickListener, true);
        mTimerSupport.unregister(onTickListener);
        mTimerSupport.register(1, onTickListener, true);
        mTimerSupport.unregister(onTickListener);
        mTimerSupport.register(1, onTickListener, true);
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testIntervalLargerThan1() {
        assertEquals(mTimerSupport.getStatus(), TimerStatus.Waiting);
        mTimerSupport.register(3, new OnTickListener() {

            long start = System.currentTimeMillis();

            @Override
            public void onTick() {
                assertEquals(Looper.myLooper(), Looper.getMainLooper());
                assertEquals(mTimerSupport.getStatus(), TimerStatus.Running);
                long end = System.currentTimeMillis();
                long time = (end - start);
                Log.d("RxTimerSupportTest", "3 testTimerExecution " + time);
                assertTrue(Math.abs(time - 3 * 1000) < 50);
                start = end;
            }
        });
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testTwoListener() {
        assertEquals(mTimerSupport.getStatus(), TimerStatus.Waiting);
        mTimerSupport.register(1, new OnTickListener() {

            long start = System.currentTimeMillis();

            @Override
            public void onTick() {
                assertEquals(Looper.myLooper(), Looper.getMainLooper());
                assertEquals(mTimerSupport.getStatus(), TimerStatus.Running);
                long end = System.currentTimeMillis();
                long time = (end - start);
                Log.d("RxTimerSupportTest", "1 testTimerExecution " + time);
                assertTrue(Math.abs(time - 1 * 1000) < 50);
                start = end;
            }
        });

        mTimerSupport.register(3, new OnTickListener() {

            long start = System.currentTimeMillis();

            @Override
            public void onTick() {
                assertEquals(Looper.myLooper(), Looper.getMainLooper());
                assertEquals(mTimerSupport.getStatus(), TimerStatus.Running);
                long end = System.currentTimeMillis();
                long time = (end - start);
                Log.d("RxTimerSupportTest", "3 testTimerExecution " + time);
                assertTrue(Math.abs(time - 3 * 1000) < 50);
                start = end;
            }
        });
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testPause() {
        assertEquals(mTimerSupport.getStatus(), TimerStatus.Waiting);
        mTimerSupport.register(1, new OnTickListener() {

            long start = System.currentTimeMillis();

            @Override
            public void onTick() {
                //should not here
                assertTrue(false);
            }
        });

        mTimerSupport.pause();
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testResume() {
        assertEquals(mTimerSupport.getStatus(), TimerStatus.Waiting);
        mTimerSupport.register(1, new OnTickListener() {

            @Override
            public void onTick() {
                assertEquals(Looper.myLooper(), Looper.getMainLooper());
                assertEquals(mTimerSupport.getStatus(), TimerStatus.Running);
                Log.d("RxTimerSupportTest", "testResume");
            }
        });

        mTimerSupport.pause();
        mTimerSupport.restart();
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testStop() {
        assertEquals(mTimerSupport.getStatus(), TimerStatus.Waiting);
        mTimerSupport.register(1, new OnTickListener() {

            @Override
            public void onTick() {
                //should not here
                assertTrue(false);
            }
        });

        mTimerSupport.register(3, new OnTickListener() {

            @Override
            public void onTick() {
                //should not here
                assertTrue(false);
            }
        });
        mTimerSupport.clear();
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testObservableApi() {
        mTimerSupport.getTickObservable(2, false).subscribe(new Consumer<Long>() {

            long start = System.currentTimeMillis();

            @Override
            public void accept(Long aLong) throws Exception {
                long end = System.currentTimeMillis();
                long time = (end - start);
                Log.d("RxTimerSupportTest", "testObservableApi " + time);
                assertTrue(Math.abs(time - 2 * 1000) < 50);
                start = end;
            }
        });
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testObservableApi2() {
        final TestConsumer<Long> testConsumer = new TestConsumer<>();
        mTimerSupport.getTickObservable(2, 5, false)
            .doOnComplete(new Action() {
                @Override
                public void run() throws Exception {
                    Log.d("RxTimerSupportTest", "testObservableApi2 onComlete");
                    assertTrue(testConsumer.getCount() == 5);
                }
            })
            .subscribe(testConsumer);
    }

    private class TestConsumer<T> implements Consumer<T> {

        private int count = 0;

        public int getCount() {
            return count;
        }

        @Override
        public void accept(T o) throws Exception {
            count++;
            Log.d("RxTimerSupportTest", "TestConsumer count = " + count);
        }
    }

}
