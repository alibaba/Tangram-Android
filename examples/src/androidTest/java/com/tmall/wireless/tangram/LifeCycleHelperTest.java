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

import java.util.concurrent.TimeUnit;

import com.alibaba.android.rx.lifecycle.ActivityLFEvent;
import com.alibaba.android.rx.lifecycle.LifeCycleProviderImpl;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.SmallTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by longerian on 2018/4/8.
 *
 * @author longerian
 * @date 2018/04/08
 */
@RunWith(AndroidJUnit4.class)
public class LifeCycleHelperTest {

    @Rule
    public final UiThreadTestRule uiThread = new UiThreadTestRule();

    private final Context context = InstrumentationRegistry.getContext();

    private Observable<Long> mObservable;

    private TestObserver<Long> mObserver;

    private TestScheduler mTestScheduler;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mObservable = Observable.interval(1, TimeUnit.MINUTES, mTestScheduler);
        mObserver = new TestObserver<Long>();
    }

    @Test
    @SmallTest
    @UiThreadTest
    public void testBindUntil() {
        LifeCycleProviderImpl<ActivityLFEvent> activityEventLifeCycleProvider = new LifeCycleProviderImpl<>(
            ActivityLFEvent.ACTIVITY_LIFECYCLE);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.CREATE);

        mObservable.compose(activityEventLifeCycleProvider.<Long>bindUntil(ActivityLFEvent.STOP)).subscribe(mObserver);

        mObserver.assertSubscribed();
        mObserver.assertEmpty();

        mTestScheduler.advanceTimeTo(70, TimeUnit.SECONDS);
        mObserver.assertValues(0L);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.START);

        mTestScheduler.advanceTimeTo(6, TimeUnit.MINUTES);
        mObserver.assertValues(0L, 1L, 2L, 3L, 4L, 5L);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.RESUME);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.PAUSE);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.STOP);

        mObserver.assertComplete();

        mTestScheduler.advanceTimeTo(10, TimeUnit.MINUTES);

        mObserver.assertValueCount(6);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.DESTROY);
    }

    @Test
    @UiThreadTest
    @SmallTest
    public void testBindToLifecycle() {
        LifeCycleProviderImpl<ActivityLFEvent> activityEventLifeCycleProvider = new LifeCycleProviderImpl<>(
            ActivityLFEvent.ACTIVITY_LIFECYCLE);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.CREATE);

        mObservable.compose(activityEventLifeCycleProvider.<Long>bindToLifecycle()).subscribe(mObserver);

        mTestScheduler.advanceTimeTo(70, TimeUnit.SECONDS);

        mObserver.assertValues(0L);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.START);

        mTestScheduler.advanceTimeTo(6, TimeUnit.MINUTES);

        mObserver.assertValues(0L, 1L, 2L, 3L, 4L, 5L);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.RESUME);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.PAUSE);

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.STOP);

        mObserver.assertNotComplete();

        activityEventLifeCycleProvider.emitNext(ActivityLFEvent.DESTROY);

        mObserver.assertComplete();
    }

    @Test
    @UiThreadTest
    @SmallTest
    public void testCustomLifecycleEvent() {
        LifeCycleProviderImpl<CellBindLFEvent> customLifecycleProvider = new LifeCycleProviderImpl<>(
            CellBindLFEvent.CELLBIND_LIFECYCLE);

        mObservable.compose(customLifecycleProvider.<Long>bindUntil(CellBindLFEvent.UNBIND)).subscribe(mObserver);

        mObserver.assertEmpty();

        mTestScheduler.advanceTimeTo(70, TimeUnit.SECONDS);

        mObserver.assertValue(0L);

        customLifecycleProvider.emitNext(CellBindLFEvent.PRE_BIND);

        mTestScheduler.advanceTimeTo(2, TimeUnit.MINUTES);

        mObserver.assertValues(0L, 1L);

        customLifecycleProvider.emitNext(CellBindLFEvent.BIND);

        customLifecycleProvider.emitNext(CellBindLFEvent.POST_BIND);

        mTestScheduler.advanceTimeTo(3, TimeUnit.MINUTES);

        mObserver.assertValues(0L, 1L, 2L);

        customLifecycleProvider.emitNext(CellBindLFEvent.PRE_UNBIND);

        customLifecycleProvider.emitNext(CellBindLFEvent.UNBIND);

        mTestScheduler.advanceTimeTo(4, TimeUnit.MINUTES);

        mObserver.assertValues(0L, 1L, 2L);

        mObserver.assertComplete();

        customLifecycleProvider.emitNext(CellBindLFEvent.POST_UNBIND);
    }


}
