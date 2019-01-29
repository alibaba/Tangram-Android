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

package com.tmall.wireless.tangram3;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.View;

import com.tmall.wireless.tangram3.dataparser.concrete.Card;
import com.tmall.wireless.tangram3.structure.BaseCell;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mikeafc on 16/4/26.
 */
public class MVResolver {
    private ArrayMap<String, Class<? extends View>> typeViewMap = new ArrayMap<>(64);

    private ArrayMap<String, Class<? extends BaseCell>> typeCellMap = new ArrayMap(64);

    private HashMap<String, Card> idCardMap = new HashMap<>();

    private ArrayMap<BaseCell, View> mvMap = new ArrayMap<>(128);

    private ArrayMap<View, BaseCell> vmMap = new ArrayMap<>(128);

    public void register(String type, Class<? extends View> viewClazz) {
        typeViewMap.put(type, viewClazz);
    }

    public void register(BaseCell cell, View view) {
        mvMap.put(cell, view);
        vmMap.put(view, cell);
    }

    public void setCards(List<Card> list) {
        synchronized (idCardMap) {
            for (Card card : list) {
                if (!TextUtils.isEmpty(card.id)) {
                    idCardMap.put(card.id, card);
                }
            }
        }
    }

    public Card findCardById(String id) {
        synchronized (idCardMap) {
            return idCardMap.get(id);
        }
    }

    public void reset() {
        mvMap.clear();
        vmMap.clear();
    }

    public View getView(BaseCell cell) {
        return mvMap.get(cell);
    }

    @Deprecated
    public View getView(String uniqueId) {
        return null;
    }

    public BaseCell getCell(View view) {
        return vmMap.get(view);
    }

    public Class<? extends View> getViewClass(String type) {
        return typeViewMap.get(type);
    }
}
