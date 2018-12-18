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

package com.tmall.wireless.tangram3.eventbus;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by longerian on 16/4/29.
 * <p>
 * Used to wrap an event subscriber.
 */
public class EventHandlerWrapper {

    /**
     * Interested event type
     */
    @NonNull
    protected final String type;

    /**
     * Interested event producer's Id
     */
    protected final String producer;

    /**
     * Real subscriber object to invoke callback method
     */
    @NonNull
    protected final Object subscriber;

    /**
     * Callback mehtod's name in subscriber object, the method must be provided with parameter type of {@link Event}.
     */
    protected final String action;

    /**
     * Cached callback method object.
     */
    protected final Method handlerMethod;

    protected final IEventHandlerReceiver eventHandlerReceiver;

    protected String eventId;

    EventHandlerWrapper(@NonNull String type, String producer, @NonNull Object subscriber,
                        String action) {
        this.type = type;
        this.producer = producer;
        this.subscriber = subscriber;
        this.action = action;
        this.handlerMethod = ReflectedActionFinder.findMethodByName(action, subscriber);
        this.eventHandlerReceiver = null;
    }

    public EventHandlerWrapper(String eventId, String type, IEventHandlerReceiver eventHandlerReceiver) {
        this.eventId = eventId;
        this.type = type;
        this.producer = null;
        this.subscriber = null;
        this.action = null;
        this.handlerMethod = null;
        this.eventHandlerReceiver = eventHandlerReceiver;
    }

    final protected void handleEvent(@NonNull Event event) {
        if (handlerMethod != null) {
            try {
                handlerMethod.invoke(subscriber, event);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else if (eventHandlerReceiver != null) {
            eventHandlerReceiver.handleEvent(event);
        }
    }

}
