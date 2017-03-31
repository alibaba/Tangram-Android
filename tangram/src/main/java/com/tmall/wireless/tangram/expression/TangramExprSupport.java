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

import android.support.v4.util.ArrayMap;

/**
 * Created by longerian on 16/8/23.
 */

final public class TangramExprSupport implements ITangramExprParser {

    public static final String VAR_START = "$";

    public static final String TANGRAM = "$tangram";

    private ArrayMap<String, ITangramExprParser> exprParsers = new ArrayMap<String, ITangramExprParser>();

    public TangramExprSupport() {
    }

    /**
     * register a var parser, the module name must be start with $
     * @param module
     * @param parser
     */
    public void registerExprParser(String module, ITangramExprParser parser) {
        if (module != null && module.startsWith(VAR_START)) {
            exprParsers.put(module, parser);
        }
    }

    public void unregisterExprParser(String module) {
        if (module != null) {
            exprParsers.remove(module);
        }
    }

    public ITangramExprParser getExprParser(String module) {
        if (module != null) {
            return exprParsers.get(module);
        }
        return null;
    }

    @Override
    public Object getValueBy(TangramExpr var) {
        if (var.hasNextFragment()) {
            String next = var.nextFragment();
            ITangramExprParser registeredParser = exprParsers.get(next);
            if (registeredParser != null) {
                return registeredParser.getValueBy(var);
            } else {
                return null;
            }
        }
        return null;
    }
}
