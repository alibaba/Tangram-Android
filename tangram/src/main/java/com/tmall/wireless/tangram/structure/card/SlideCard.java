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

package com.tmall.wireless.tangram.structure.card;

import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.WrapperCard;
import com.tmall.wireless.tangram.eventbus.BusSupport;
import com.tmall.wireless.tangram.eventbus.Event;
import com.tmall.wireless.tangram.eventbus.EventHandlerWrapper;
import com.tmall.wireless.tangram.structure.BaseCell;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by longerian on 17/1/27.
 */

public class SlideCard extends WrapperCard implements SwipeCard {

    public static final String KEY_INDEX = "index";

    public static final String KEY_PAGE_COUNT = "pageCount";

    private ArrayMap<String, String> args = new ArrayMap<String, String>();

    private int mIndex;

    private int mTotalPageCount;

    private Map<Integer, TabContentCache> mCacheMap = new HashMap<>();

    public SlideCard(@NonNull Card card) {
        super(card);
        mIndex = 0;
        mTotalPageCount = Integer.MAX_VALUE;
    }

    @Override
    public int getCurrentIndex() {
        return mIndex;
    }

    @Override
    public int getTotalPage() {
        return mTotalPageCount;
    }

    @Override
    public void switchTo(int index) {
        BusSupport busSupport = serviceManager.getService(BusSupport.class);
        if (busSupport != null) {
            storeCache();
            args.put(KEY_INDEX, String.valueOf(index));
            busSupport.post(BusSupport.obtainEvent("switchTo", null, args, null));
            mIndex = index;
        }
    }

    @Override
    protected void onAdded() {
        super.onAdded();
        BusSupport busSupport = serviceManager.getService(BusSupport.class);
        if (busSupport != null) {
            busSupport.register(mSetMeta);
        }
    }

    @Override
    protected void onRemoved() {
        super.onRemoved();
        BusSupport busSupport = serviceManager.getService(BusSupport.class);
        if (busSupport != null) {
            busSupport.unregister(mSetMeta);
        }
    }

    private EventHandlerWrapper mSetMeta = BusSupport
            .wrapEventHandler("setMeta", null, this, "parseMeta");

    @Keep
    public void parseMeta(Event event) {
        try {
            if (mTotalPageCount != Integer.MAX_VALUE) {
                storeCache();
            }
            mIndex = Integer.parseInt(event.args.get(KEY_INDEX));
            mTotalPageCount = Integer.parseInt(event.args.get(KEY_PAGE_COUNT));
        } catch (Exception e) {
        }
    }

    public boolean hasCacheOf(int index) {
        TabContentCache tabContentCache = mCacheMap.get(index);
        return tabContentCache != null && tabContentCache.cells != null && !tabContentCache.cells.isEmpty();
    }

    public TabContentCache getIndexCache(int index) {
        TabContentCache tabContentCache = mCacheMap.get(index);
        return tabContentCache;
    }

    private void storeCache() {
        List<BaseCell> initCells = getCells();
        BaseCell placeHolder = getPlaceholderCell();
        if (initCells != null && !initCells.isEmpty()) {
            TabContentCache tabContentCache = new TabContentCache(mIndex, initCells, placeHolder);
            tabContentCache.id = id;
            tabContentCache.loaded = loaded;
            tabContentCache.loading = loading;
            tabContentCache.page = page;
            tabContentCache.hasMore = hasMore;
            mCacheMap.put(mIndex, tabContentCache);
        }
    }

    static public final class TabContentCache {

        int index = -1;

        public String id;
        public boolean loaded = true;
        public boolean loading = false;
        public int page;
        public boolean hasMore;

        public List<BaseCell> cells;

        TabContentCache(int index, List<BaseCell> cells, BaseCell placeHolder) {
            this.index = index;
            this.cells = new ArrayList<>(cells);
            this.cells.remove(placeHolder);
        }
    }

}
