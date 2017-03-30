/*
 * MIT License
 *
 * Copyright (c) 2017 Alibaba Group
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

package com.tmall.wireless.tangram.expression;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by longerian on 16/8/23.
 */

public class TangramExpr {

    private static final int STATE_COMMON = 0;
    private static final int STATE_ARRAY_START = 1;
    private static final int STATE_ARRAY_END = 2;

    private static final char DOT = '.';
    private static final char ARRAY_START = '[';
    private static final char ARRAY_END = ']';

    private Queue<String> exprFragment = new LinkedList<String>();

    private int state;

    public TangramExpr(String path) {
        compile(path);
    }

    /**
     * support parsing $profile.device.channel\$tangram.calendar[0].title
     * @param path
     */
    private void compile(String path) {
        StringBuilder sb = new StringBuilder();
        state = STATE_COMMON;
        for (int i = 0, length = path.length(); i < length; i++) {
            char c = path.charAt(i);
            switch (c) {
                case DOT:
                    if (state == STATE_ARRAY_START) {
                        sb.append(c);
                        break;
                    } else if (state == STATE_ARRAY_END) {
                        state = STATE_COMMON;
                        break;
                    } else {
                        exprFragment.offer(sb.toString());
                        sb.delete(0, sb.length());
                    }
                    break;
                case ARRAY_START:
                    if (state == STATE_COMMON) {
                        exprFragment.offer(sb.toString());
                        sb.delete(0, sb.length());
                        state = STATE_ARRAY_START;
                    } else {
                        //error
                        return;
                    }
                    break;
                case ARRAY_END:
                    if (state == STATE_ARRAY_START) {
                        exprFragment.offer(sb.toString());
                        sb.delete(0, sb.length());
                        state = STATE_ARRAY_END;
                    } else {
                        //error
                        return;
                    }
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        if (state == STATE_COMMON) {
            exprFragment.offer(sb.toString());
        }
    }

    public boolean hasNextFragment() {
        return !exprFragment.isEmpty();
    }

    public String nextFragment() {
        return exprFragment.poll();
    }

}
