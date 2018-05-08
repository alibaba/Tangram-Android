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

import com.alibaba.android.rx.lifecycle.LFEvent;

import io.reactivex.functions.Function;

/**
 * Created by longerian on 2018/4/8.
 *
 * @author longerian
 * @date 2018/04/08
 */

public class CellBindLFEvent extends LFEvent {

    public static final CellBindLFEvent PRE_BIND = new CellBindLFEvent("onPreBind");
    public static final CellBindLFEvent BIND = new CellBindLFEvent("onBind");
    public static final CellBindLFEvent POST_BIND = new CellBindLFEvent("onPostBind");

    public static final CellBindLFEvent PRE_UNBIND = new CellBindLFEvent("onPreUnbind");
    public static final CellBindLFEvent UNBIND = new CellBindLFEvent("onUnbind");
    public static final CellBindLFEvent POST_UNBIND = new CellBindLFEvent("onPostUnbind");

    private CellBindLFEvent(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "CellBindLFEvent{" +
            "name='" + name + '\'' +
            '}';
    }

    public static final Function<CellBindLFEvent, CellBindLFEvent> CELLBIND_LIFECYCLE =
        new Function<CellBindLFEvent, CellBindLFEvent>() {
            @Override
            public CellBindLFEvent apply(CellBindLFEvent lastEvent) throws Exception {
                if (lastEvent.equals(PRE_BIND)) {
                    return PRE_UNBIND;
                }
                if (lastEvent.equals(BIND)) {
                    return UNBIND;
                }
                if (lastEvent.equals(POST_BIND)) {
                    return POST_UNBIND;
                }
                if (lastEvent.equals(PRE_UNBIND)) {
                    return UNBIND;
                }
                if (lastEvent.equals(UNBIND)) {
                    return POST_UNBIND;
                }
                throw new IllegalAccessException("Cannot bind to Activity lifecycle when outside of it.");
            }
        };
}
