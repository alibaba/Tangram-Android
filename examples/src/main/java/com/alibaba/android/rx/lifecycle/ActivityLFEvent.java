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

package com.alibaba.android.rx.lifecycle;

import io.reactivex.functions.Function;

/**
 * Created by longerian on 2018/4/8.
 *
 * @author longerian
 * @date 2018/04/08
 */

public class ActivityLFEvent extends LFEvent {

    public static final ActivityLFEvent CREATE = new ActivityLFEvent("onCreate");
    public static final ActivityLFEvent START = new ActivityLFEvent("onStart");
    public static final ActivityLFEvent RESUME = new ActivityLFEvent("onResume");
    public static final ActivityLFEvent PAUSE = new ActivityLFEvent("onPause");
    public static final ActivityLFEvent STOP = new ActivityLFEvent("onStop");
    public static final ActivityLFEvent DESTROY = new ActivityLFEvent("onDestroy");

    public static final Function<ActivityLFEvent, ActivityLFEvent> ACTIVITY_LIFECYCLE =
        new Function<ActivityLFEvent, ActivityLFEvent>() {
            @Override
            public ActivityLFEvent apply(ActivityLFEvent lastEvent) throws Exception {
                if (lastEvent.equals(CREATE)) {
                    return DESTROY;
                }
                if (lastEvent.equals(START)) {
                    return STOP;
                }
                if (lastEvent.equals(RESUME)) {
                    return PAUSE;
                }
                if (lastEvent.equals(PAUSE)) {
                    return STOP;
                }
                if (lastEvent.equals(STOP)) {
                    return DESTROY;
                }
                throw new IllegalAccessException("Cannot bind to Activity lifecycle when outside of it.");
            }
        };

    private ActivityLFEvent(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "ActivityLFEvent{" +
            "name='" + name + '\'' +
            '}';
    }
}
