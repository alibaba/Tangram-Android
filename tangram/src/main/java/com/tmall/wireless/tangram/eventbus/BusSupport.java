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

package com.tmall.wireless.tangram.eventbus;

import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by longerian on 16/4/26.
 */
public class BusSupport implements IDispatcherDelegate {

    public static final String EVENT_ON_CLICK = "onClick";

    public static final String EVENT_ON_EXPOSURE = "onExposure";

    public static final String EVENT_ON_SCROLL = "onScroll";

    private IDispatcher mDispatcher;

    private ConcurrentHashMap<String, List<EventHandlerWrapper>> subscribers = new ConcurrentHashMap<>();

    public BusSupport() {
        mDispatcher = new Dispatcher(this);
    }

    /**
     * Register an event subscriber which is wrapped by {@link EventHandlerWrapper}
     * @param eventHandler event subscriber wrapper
     */
    public synchronized void register(@NonNull EventHandlerWrapper eventHandler) {
        String type = eventHandler.type;
        List<EventHandlerWrapper> handlers = subscribers.get(eventHandler.type);
        if (handlers == null) {
            handlers = new ArrayList<>();
            subscribers.put(type, handlers);
        }
        handlers.add(eventHandler);
    }

    /**
     * Unregister an event subscriber
     * @param eventHandler event subscriber wrapper
     */
    public synchronized void unregister(@NonNull EventHandlerWrapper eventHandler) {
        String type = eventHandler.type;
        List<EventHandlerWrapper> handlers = subscribers.get(type);
        if (handlers != null) {
            handlers.remove(eventHandler);
        }
    }

    /**
     * Shutdown event bus, this is called when Tangram is destroyed. All event subscribers are cleared and pending
     * event are canceled.
     */
    public void shutdown() {
        subscribers.clear();
        mDispatcher.stopSelf();
        ReflectedActionFinder.clear();
    }

    /**
     * Post an event to the bus, called by event sender
     * @param event Event object
     * @return Return true if the event is successfully enqueued into the event queue.
     */
    public boolean post(@NonNull Event event) {
        return mDispatcher.enqueue(event);
    }

    /**
     * Post a list of events to the bus, called by event sender
     * @param eventList Event object list
     * @return Return true if the events are successfully enqueued into the event queue.
     */
    public boolean post(@NonNull List<Event> eventList) {
        return mDispatcher.enqueue(eventList);
    }

    /**
     * Dispatch event to a subscriber, you should not call this method directly.
     * @param event Event object
     */
    @Override
    public synchronized void dispatch(@NonNull Event event) {
        String type = event.type;
        List<EventHandlerWrapper> eventHandlers = subscribers.get(type);
        if (eventHandlers != null) {
            EventHandlerWrapper handler = null;
            for (int i = 0, size = eventHandlers.size(); i < size; i++) {
                handler = eventHandlers.get(i);
                if (!TextUtils.isEmpty(handler.producer) && handler.producer.equals(event.sourceId)) {
                    handler.handleEvent(event);
                } else if (TextUtils.isEmpty(handler.producer)) {
                    handler.handleEvent(event);
                }
            }
        }
    }

    /**
     * @return An event object from recycler pool. It is suggested to use this method to obtain an event object. The
     * returned object is blank.
     */
    public static Event obtainEvent() {
        return EventPool.sharedInstance().acquire();
    }

    /**
     *
     * @param type Event type to identify an event.
     * @param sourceId Event sender's unique id. If sourceId is empty, the event would be dispatched to any
     *                 subscribers which is registered to receive events with 'type'. Otherwise, the event would be
     *                 dispatched to any subscribers which is registered to receive events with 'type' and this 'sourceId'.
     * @param args Event args, may be null.
     * @param eventContext Event context, see {@link EventContext}, may by null.
     * @return An event object from recycler pool. It is suggested to use this method to obtain an event object. The
     * returned object's field is filled provided params.
     */
    public static Event obtainEvent(String type, String sourceId,
            ArrayMap<String, String> args, EventContext eventContext) {
        Event event = EventPool.sharedInstance().acquire();
        event.type = type;
        event.sourceId = sourceId;
        event.args = args;
        event.eventContext = eventContext;
        return event;
    }

    /**
     * See {@link EventHandlerWrapper}
     * @param type The event type subcriber is interested.
     * @param producer The event source id subscriber is interested.
     * @param subscriber Original subscriber object.
     * @param action The name of callback method with parameter 'Event'. If empty, the subscribe must provide a handler method named 'execute'. See {@link ReflectedActionFinder}
     * @return An EventHandlerWrapper wrapping a subscriber and used to registered into event bus.
     */
    public static EventHandlerWrapper wrapEventHandler(@NonNull String type, String producer, @NonNull Object subscriber,
            String action) {
        return new EventHandlerWrapper(type, producer, subscriber, action);
    }

    /**
     * This performs the same feature as {@link #wrapEventHandler(String, String, Object, String)}, just parse the params from jsonObject.
     * @param subscriber Original subscriber object
     * @param jsonObject Json params
     * @return An EventHandlerWrapper wrapping a subscriber and used to registered into event bus.
     */
    public static EventHandlerWrapper wrapEventHandler(@NonNull Object subscriber, @NonNull JSONObject jsonObject) {
        String type = jsonObject.optString("type");
        if (TextUtils.isEmpty(type)) {
            return null;
        }
        String producer = jsonObject.optString("producer");
//        String subscriber = jsonObject.optString("subscriber");
        String action = jsonObject.optString("action");
        return new EventHandlerWrapper(type, producer, subscriber, action);
    }

}
