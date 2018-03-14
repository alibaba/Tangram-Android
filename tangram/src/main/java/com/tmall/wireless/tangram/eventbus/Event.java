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

import android.support.v4.util.ArrayMap;

/**
 * Created by longerian on 16/4/26.
 */
public class Event {

    /**
     * Event type
     */
    public String type;

    /**
     * Event producer's Id
     */
    public String sourceId;

    /**
     * Event args
     */
    public ArrayMap<String, String> args;

    /**
     * Context
     */
    public EventContext eventContext;

    public Event() {
        args = new ArrayMap<>();
    }

    /**
     * Append arg to map
     * @param key
     * @param value
     */
    public void appendArg(String key, String value) {
        if (args != null) {
            args.put(key, value);
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "args=" + args +
                ", sourceId='" + sourceId + '\'' +
                ", type=" + type +
                '}';
    }
}
