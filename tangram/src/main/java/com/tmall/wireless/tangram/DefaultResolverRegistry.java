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

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.SparseArray;
import android.view.View;

import com.tmall.wireless.tangram.dataparser.concrete.BaseCardBinderResolver;
import com.tmall.wireless.tangram.dataparser.concrete.BaseCellBinder;
import com.tmall.wireless.tangram.dataparser.concrete.BaseCellBinderResolver;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.CardResolver;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.card.VVCard;
import com.tmall.wireless.tangram.structure.viewcreator.ViewHolderCreator;

/**
 * The place to register cell and card, all build-in and user cell, card ared registed here finally.
 */
public class DefaultResolverRegistry {
    final CardResolver mDefaultCardResolver = new CardResolver();

    final BaseCellBinderResolver mDefaultCellBinderResolver = new BaseCellBinderResolver();
    final BaseCardBinderResolver mDefaultCardBinderResolver = new BaseCardBinderResolver(mDefaultCardResolver);

    ArrayMap<String, ViewHolderCreator> viewHolderMap = new ArrayMap<>(64);

    MVHelper mMVHelper;

    public void setMVHelper(MVHelper mMvHelper) {
        this.mMVHelper = mMvHelper;
    }

    public MVHelper getMVHelper() {
        return mMVHelper;
    }

    /**
     * register cell with custom model class and view class
     * @param type
     * @param cellClz
     * @param viewClz
     * @param <V>
     */
    public <V extends View> void registerCell(String type, final @NonNull Class<? extends BaseCell> cellClz, final @NonNull Class<V> viewClz) {
        registerCell(type, viewClz);
        mMVHelper.resolver().registerCompatible(type, cellClz);
    }

    /**
     * register cell with custom model class and view creator
     * @param type
     * @param cellClz
     * @param viewHolderCreator
     * @param <V>
     */
    public <V extends View> void registerCell(String type, @NonNull Class<? extends BaseCell> cellClz, @NonNull ViewHolderCreator viewHolderCreator) {
        viewHolderMap.put(type, viewHolderCreator);
        registerCell(type, cellClz, viewHolderCreator.viewClz);
    }

    /**
     * register cell with custom view class, the model of cell is provided with default type
     * @param type
     * @param viewClz
     * @param <V>
     */
    public <V extends View> void registerCell(String type, final @NonNull Class<V> viewClz) {
        if (viewHolderMap.get(type) == null) {
            mDefaultCellBinderResolver.register(type, new BaseCellBinder<>(viewClz, mMVHelper));
        } else {
            mDefaultCellBinderResolver.register(type, new BaseCellBinder<ViewHolderCreator.ViewHolder, V>(viewHolderMap.get(type),
                mMVHelper));
        }
        mMVHelper.resolver().register(type, viewClz);
    }

    /**
     * register card with type and card class
     * @param type
     * @param cardClz
     */
    public void registerCard(String type, Class<? extends Card> cardClz) {
        mDefaultCardResolver.register(type, cardClz);
    }

    /**
     * register item render by virtual view* @param type
     * */
    public <V extends View> void registerVirtualView(String type) {
        mDefaultCellBinderResolver.register(type, new BaseCellBinder<>(type, mMVHelper));
        registerCard(type, VVCard.class);
    }

    public CardResolver getDefaultCardResolver() {
        return mDefaultCardResolver;
    }


    public BaseCellBinderResolver getDefaultCellBinderResolver() {
        return mDefaultCellBinderResolver;
    }

    public BaseCardBinderResolver getDefaultCardBinderResolver() {
        return mDefaultCardBinderResolver;

    }
}
