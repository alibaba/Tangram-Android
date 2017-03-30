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

package com.tmall.wireless.tangram.support;

import android.view.View;

import com.tmall.wireless.tangram.structure.BaseCell;

/**
 * A helper class supports binding cell data to a view
 * <br />
 * Created by mikeafc on 16/4/30.
 */
public abstract class CellSupport {
    /***
     * Whether the cell is valid, if not, the cell will not be display on screen
     *
     * @param cell
     * @return display or not
     */
    public abstract boolean isValid(BaseCell cell);

    /***
     * Invoked when view would be display on screen, and would be invoked first
     *
     * @param cell
     * @param view
     */
    public void bindView(BaseCell cell, View view) {

    }

    /***
     * Invoked when view would be display on screen, and would be invoked after render view finish
     *
     * @param cell
     * @param view
     */
    public void postBindView(BaseCell cell, View view) {

    }

    /***
     * Invoked when view would be disappeared on screen, and would be invoked first
     *
     * @param cell
     * @param view
     */
    public void unBindView(BaseCell cell, View view) {

    }

    public void onCellRemoved(BaseCell cell) {

    }

    /***
     * Invoked when an exception thrown during view binding
     *
     * @param cell
     * @param view
     * @param e
     */
    public void onBindViewException(BaseCell cell, View view, Exception e) {

    }
}
