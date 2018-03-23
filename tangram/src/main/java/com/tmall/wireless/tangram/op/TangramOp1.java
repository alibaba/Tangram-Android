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

package com.tmall.wireless.tangram.op;

/**
 * Created by longerian on 2018/3/23.
 *
 * @author longerian
 * @date 2018/03/23
 */

public class TangramOp1<V1> {

    public TangramOp1(int type, V1 arg1) {
        mType = type;
        this.arg1 = arg1;
    }

    public static class OP_TYPE {
        public static final int INSERT_CELL = 0;
        public static final int INSERT_CELL_LIST = 1;
        public static final int INSERT_GROUP = 2;
        public static final int INSERT_GROUP_LIST = 3;
        public static final int APPEND_GROUP = 4;
        public static final int APPEND_GROUP_LIST = 5;
        public static final int REMOVE_CELL_POSITION = 6;
        public static final int REMOVE_CELL = 7;
        public static final int REMOVE_GROUP_IDX = 8;
        public static final int REMOVE_GROUP = 9;
        public static final int REPLACE_CELL = 10;
        public static final int REPLACE_GROUP_CONTENT = 11;
        public static final int REPLACE_GROUP = 12;
        public static final int UPDATE_CELL = 13;
    }

    private final int mType;

    private final V1 arg1;

    public int getType() {
        return mType;
    }

    public V1 getArg1() {
        return arg1;
    }

}
